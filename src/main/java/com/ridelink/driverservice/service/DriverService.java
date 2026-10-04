package com.ridelink.driverservice.service;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.exception.ConflictException;
import com.ridelink.driverservice.exception.ForbiddenException;
import com.ridelink.driverservice.exception.ResourceNotFoundException;
import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.Driver;
import com.ridelink.driverservice.model.OperationalStatus;
import com.ridelink.driverservice.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse createDriver(DriverCreateRequest request, String requestingAccountId, String requestingRole) {
        log.info("Creating operational driver profile for accountId: {}", request.getAccountId());

        boolean isAdmin = "ROLE_ADMIN".equals(requestingRole);
        if (!isAdmin && requestingAccountId != null && !requestingAccountId.equals(request.getAccountId())) {
            throw new ForbiddenException("Cannot create driver profile for another user's account");
        }

        if (driverRepository.existsByAccountId(request.getAccountId())) {
            throw new ConflictException("Driver profile already exists for account ID: " + request.getAccountId());
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new ConflictException("License number already registered: " + request.getLicenseNumber());
        }

        Driver driver = new Driver();
        driver.setAccountId(request.getAccountId());
        driver.setLicenseNumber(request.getLicenseNumber().trim());
        driver.setServiceArea(request.getServiceArea().trim());
        driver.setCurrentLatitude(request.getCurrentLatitude());
        driver.setCurrentLongitude(request.getCurrentLongitude());
        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        driver.setOperationalStatus(OperationalStatus.ACTIVE);
        Instant now = Instant.now();
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);

        Driver saved = driverRepository.save(driver);
        log.info("Driver profile created with ID: {}", saved.getId());
        return DriverResponse.fromEntity(saved);
    }

    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));
        return DriverResponse.fromEntity(driver);
    }

    public DriverResponse getDriverByAccountId(String accountId) {
        Driver driver = driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for account ID: " + accountId));
        return DriverResponse.fromEntity(driver);
    }

    public DriverResponse updateDriver(String id, DriverUpdateRequest request, String requestingAccountId, String requestingRole) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        if (!driver.getLicenseNumber().equalsIgnoreCase(request.getLicenseNumber().trim())
                && driverRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) {
            throw new ConflictException("License number already registered: " + request.getLicenseNumber());
        }

        driver.setLicenseNumber(request.getLicenseNumber().trim());
        driver.setServiceArea(request.getServiceArea().trim());
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse updateAvailability(String id, DriverAvailabilityUpdateRequest request,
                                             String requestingAccountId, String requestingRole) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        driver.setAvailabilityStatus(request.getAvailabilityStatus());
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        log.info("Driver ID: {} availability updated to: {}", id, request.getAvailabilityStatus());
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse updateLocation(String id, DriverLocationUpdateRequest request,
                                         String requestingAccountId, String requestingRole) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        driver.setCurrentLatitude(request.getCurrentLatitude());
        driver.setCurrentLongitude(request.getCurrentLongitude());
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        log.info("Driver ID: {} location updated to lat: {}, lon: {}", id, request.getCurrentLatitude(), request.getCurrentLongitude());
        return DriverResponse.fromEntity(updated);
    }

    public List<DriverResponse> getAvailableDrivers() {
        return driverRepository.findByOperationalStatusAndAvailabilityStatus(
                        OperationalStatus.ACTIVE, AvailabilityStatus.AVAILABLE)
                .stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EligibleDriverResponse> getEligibleDrivers(Double pickupLat, Double pickupLon, String serviceArea, Double maxRadiusKm) {
        double radius = (maxRadiusKm != null && maxRadiusKm > 0) ? maxRadiusKm : 15.0;

        List<Driver> activeAvailable = driverRepository.findByOperationalStatusAndAvailabilityStatus(
                OperationalStatus.ACTIVE, AvailabilityStatus.AVAILABLE);

        if (pickupLat != null && pickupLon != null) {
            return activeAvailable.stream()
                    .filter(d -> d.getCurrentLatitude() != null && d.getCurrentLongitude() != null)
                    .map(d -> {
                        double distance = calculateHaversineDistance(
                                pickupLat, pickupLon, d.getCurrentLatitude(), d.getCurrentLongitude());
                        double roundedDistance = Math.round(distance * 100.0) / 100.0;
                        return new EligibleDriverResponse(
                                d.getId(),
                                d.getAccountId(),
                                d.getLicenseNumber(),
                                d.getServiceArea(),
                                d.getCurrentLatitude(),
                                d.getCurrentLongitude(),
                                roundedDistance,
                                d.getAvailabilityStatus(),
                                d.getOperationalStatus()
                        );
                    })
                    .filter(res -> res.getDistanceKm() <= radius)
                    .filter(res -> serviceArea == null || serviceArea.isBlank() || (res.getServiceArea() != null && res.getServiceArea().equalsIgnoreCase(serviceArea.trim())))
                    .sorted(Comparator.comparingDouble(EligibleDriverResponse::getDistanceKm))
                    .collect(Collectors.toList());
        }

        return activeAvailable.stream()
                .filter(d -> serviceArea == null || serviceArea.isBlank() || (d.getServiceArea() != null && d.getServiceArea().equalsIgnoreCase(serviceArea.trim())))
                .map(d -> new EligibleDriverResponse(
                        d.getId(),
                        d.getAccountId(),
                        d.getLicenseNumber(),
                        d.getServiceArea(),
                        d.getCurrentLatitude(),
                        d.getCurrentLongitude(),
                        0.0,
                        d.getAvailabilityStatus(),
                        d.getOperationalStatus()
                ))
                .collect(Collectors.toList());
    }

    private void verifyDriverOwnership(Driver driver, String requestingAccountId, String requestingRole) {
        boolean isAdmin = "ROLE_ADMIN".equals(requestingRole);
        boolean isOwner = driver.getAccountId() != null && driver.getAccountId().equals(requestingAccountId);

        if (!isAdmin && !isOwner) {
            log.warn("Unauthorized driver modification attempt by accountId: {} on driverId: {}", requestingAccountId, driver.getId());
            throw new ForbiddenException("Access denied: You do not own this driver operational profile");
        }
    }

    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double originLat = Math.toRadians(lat1);
        double targetLat = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(originLat) * Math.cos(targetLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
