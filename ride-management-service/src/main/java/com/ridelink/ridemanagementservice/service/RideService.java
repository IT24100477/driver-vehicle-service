package com.ridelink.ridemanagementservice.service;

import com.ridelink.ridemanagementservice.client.DriverServiceClient;
import com.ridelink.ridemanagementservice.client.FareServiceClient;
import com.ridelink.ridemanagementservice.client.dto.EligibleDriverDto;
import com.ridelink.ridemanagementservice.client.dto.FareCalculateResponseDto;
import com.ridelink.ridemanagementservice.client.dto.FareEstimateResponseDto;
import com.ridelink.ridemanagementservice.dto.*;
import com.ridelink.ridemanagementservice.exception.ForbiddenException;
import com.ridelink.ridemanagementservice.exception.InvalidRideStatusTransitionException;
import com.ridelink.ridemanagementservice.exception.NoDriverAvailableException;
import com.ridelink.ridemanagementservice.exception.ResourceNotFoundException;
import com.ridelink.ridemanagementservice.model.Location;
import com.ridelink.ridemanagementservice.model.Ride;
import com.ridelink.ridemanagementservice.model.RideStatus;
import com.ridelink.ridemanagementservice.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FareServiceClient fareServiceClient;

    public RideService(RideRepository rideRepository,
                       DriverServiceClient driverServiceClient,
                       FareServiceClient fareServiceClient) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.fareServiceClient = fareServiceClient;
    }

    public RideResponse createRide(CreateRideRequest request, String requestingAccountId, String requestingRole) {
        log.info("Creating ride request for passengerId: {}", request.getPassengerId());

        boolean isAdmin = "ROLE_ADMIN".equals(requestingRole);
        if (!isAdmin && requestingAccountId != null && !requestingAccountId.equals(request.getPassengerId())) {
            throw new ForbiddenException("Cannot book a ride for another passenger's account");
        }

        double distanceKm = (request.getEstimatedDistanceKm() != null && request.getEstimatedDistanceKm() > 0)
                ? request.getEstimatedDistanceKm()
                : calculateSimulatedDistance(request.getPickupLocation(), request.getDestinationLocation());

        int durationMinutes = (request.getEstimatedDurationMinutes() != null && request.getEstimatedDurationMinutes() > 0)
                ? request.getEstimatedDurationMinutes()
                : (int) Math.max(5, Math.round(distanceKm * 2.5));

        // Inter-service Interaction 2: Synchronous Fare Estimation call to Fare & Payment Service
        Double estimatedFareAmount = null;
        try {
            FareEstimateResponseDto fareEstimate = fareServiceClient.estimateFare(distanceKm, durationMinutes);
            if (fareEstimate != null) {
                estimatedFareAmount = fareEstimate.getTotalFare();
            }
        } catch (Exception e) {
            log.warn("Fare Service estimate call had an issue: {}. Proceeding with default estimation.", e.getMessage());
            estimatedFareAmount = 300.0 + (distanceKm * 100.0) + (durationMinutes * 20.0);
        }

        Ride ride = new Ride();
        ride.setPassengerId(request.getPassengerId());
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDestinationLocation(request.getDestinationLocation());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
        ride.setEstimatedDurationMinutes(durationMinutes);
        ride.setEstimatedFare(estimatedFareAmount);
        ride.setCreatedAt(Instant.now());

        // Inter-service Interaction 1: Auto-discover eligible available driver if requested
        if (Boolean.TRUE.equals(request.getAutoAssignDriver())) {
            assignEligibleDriver(ride);
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride created with ID: {}, status: {}", saved.getId(), saved.getStatus());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse assignDriver(String rideId, AssignDriverRequest request, String requestingAccountId, String requestingRole) {
        Ride ride = getRideEntity(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStatusTransitionException(
                    "Cannot assign driver to ride in status " + ride.getStatus() + ". Ride must be in REQUESTED status.");
        }

        if (request != null && request.getDriverId() != null && !request.getDriverId().isBlank()) {
            ride.setDriverId(request.getDriverId().trim());
            ride.setStatus(RideStatus.ASSIGNED);
            ride.setAssignedAt(Instant.now());
        } else {
            assignEligibleDriver(ride);
        }

        Ride saved = rideRepository.save(ride);
        log.info("Driver {} assigned to ride ID: {}", saved.getDriverId(), saved.getId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse acceptRide(String rideId, String requestingAccountId, String requestingRole) {
        Ride ride = getRideEntity(rideId);

        // Strict state transition: REQUESTED -> ASSIGNED -> ACCEPTED
        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new InvalidRideStatusTransitionException(ride.getStatus(), RideStatus.ACCEPTED);
        }

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());

        Ride saved = rideRepository.save(ride);
        log.info("Ride ID: {} accepted by driver: {}", saved.getId(), saved.getDriverId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse startRide(String rideId, String requestingAccountId, String requestingRole) {
        Ride ride = getRideEntity(rideId);

        // Strict state transition: ACCEPTED -> IN_PROGRESS
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusTransitionException(ride.getStatus(), RideStatus.IN_PROGRESS);
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());

        Ride saved = rideRepository.save(ride);
        log.info("Ride ID: {} started by driver: {}", saved.getId(), saved.getDriverId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse completeRide(String rideId, RideCompleteRequest request, String requestingAccountId, String requestingRole) {
        Ride ride = getRideEntity(rideId);

        // Strict state transition: IN_PROGRESS -> COMPLETED
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStatusTransitionException(ride.getStatus(), RideStatus.COMPLETED);
        }

        double finalDistance = (request != null && request.getActualDistanceKm() != null)
                ? request.getActualDistanceKm()
                : (ride.getEstimatedDistanceKm() != null ? ride.getEstimatedDistanceKm() : 10.0);

        int finalDuration = (request != null && request.getActualDurationMinutes() != null)
                ? request.getActualDurationMinutes()
                : (ride.getEstimatedDurationMinutes() != null ? ride.getEstimatedDurationMinutes() : 20);

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());

        // Inter-service Interaction 2: Call Fare & Payment Service to calculate deterministic final fare
        try {
            FareCalculateResponseDto fareResponse = fareServiceClient.calculateFare(rideId, finalDistance, finalDuration);
            if (fareResponse != null) {
                ride.setFinalFare(fareResponse.getTotalFare());
            }
        } catch (Exception e) {
            log.error("Failed to calculate final fare via Fare Service: {}. Using local calculation.", e.getMessage());
            ride.setFinalFare(300.0 + (finalDistance * 100.0) + (finalDuration * 20.0));
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride ID: {} completed. Final fare: {}", saved.getId(), saved.getFinalFare());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse cancelRide(String rideId, CancelRideRequest request, String requestingAccountId, String requestingRole) {
        Ride ride = getRideEntity(rideId);

        // Cancellation business rules: only allowed prior to trip start
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStatusTransitionException("Cannot cancel a ride that is already COMPLETED");
        }
        if (ride.getStatus() == RideStatus.IN_PROGRESS) {
            throw new InvalidRideStatusTransitionException("Cannot cancel a ride that is currently IN_PROGRESS");
        }
        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidRideStatusTransitionException("Ride is already CANCELLED");
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(request != null ? request.getCancellationReason() : "Cancelled by user");
        ride.setCancelledAt(Instant.now());

        Ride saved = rideRepository.save(ride);
        log.info("Ride ID: {} cancelled. Reason: {}", saved.getId(), saved.getCancellationReason());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse updateRideStatus(String rideId, RideStatusUpdateRequest request, String requestingAccountId, String requestingRole) {
        RideStatus target = request.getStatus();

        switch (target) {
            case ACCEPTED -> {
                return acceptRide(rideId, requestingAccountId, requestingRole);
            }
            case IN_PROGRESS -> {
                return startRide(rideId, requestingAccountId, requestingRole);
            }
            case COMPLETED -> {
                RideCompleteRequest completeReq = new RideCompleteRequest(request.getActualDistanceKm(), request.getActualDurationMinutes());
                return completeRide(rideId, completeReq, requestingAccountId, requestingRole);
            }
            case CANCELLED -> {
                CancelRideRequest cancelReq = new CancelRideRequest(request.getCancellationReason());
                return cancelRide(rideId, cancelReq, requestingAccountId, requestingRole);
            }
            default -> throw new InvalidRideStatusTransitionException("Unsupported direct status transition to: " + target);
        }
    }

    public RideResponse getRideById(String id) {
        Ride ride = getRideEntity(id);
        return RideResponse.fromEntity(ride);
    }

    public List<RideResponse> getRidesByPassengerId(String passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream()
                .map(RideResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RideResponse> getRidesByDriverId(String driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(RideResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void assignEligibleDriver(Ride ride) {
        Double lat = (ride.getPickupLocation() != null) ? ride.getPickupLocation().getLatitude() : null;
        Double lon = (ride.getPickupLocation() != null) ? ride.getPickupLocation().getLongitude() : null;
        String area = (ride.getPickupLocation() != null) ? ride.getPickupLocation().getArea() : null;

        List<EligibleDriverDto> eligibleDrivers = driverServiceClient.getEligibleDrivers(lat, lon, area, 15.0);

        if (eligibleDrivers == null || eligibleDrivers.isEmpty()) {
            log.warn("No eligible drivers available for pickup location: ({}, {}) area: {}", lat, lon, area);
            throw new NoDriverAvailableException("No available drivers found in the designated pickup service area");
        }

        EligibleDriverDto assignedDriver = eligibleDrivers.get(0);
        ride.setDriverId(assignedDriver.getDriverId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        log.info("Assigned driver {} (distance: {} km) to ride", assignedDriver.getDriverId(), assignedDriver.getDistanceKm());
    }

    private Ride getRideEntity(String id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + id));
    }

    private double calculateSimulatedDistance(Location pickup, Location destination) {
        if (pickup == null || destination == null) {
            return 8.0;
        }
        if (pickup.getLatitude() == null || destination.getLatitude() == null) {
            return 8.0;
        }
        double dLat = Math.toRadians(destination.getLatitude() - pickup.getLatitude());
        double dLon = Math.toRadians(destination.getLongitude() - pickup.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                        Math.cos(Math.toRadians(pickup.getLatitude())) * Math.cos(Math.toRadians(destination.getLatitude()));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.max(1.0, 6371.0 * c);
    }
}
