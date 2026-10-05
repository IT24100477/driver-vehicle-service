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
import com.ridelink.ridemanagementservice.model.Location;
import com.ridelink.ridemanagementservice.model.Ride;
import com.ridelink.ridemanagementservice.model.RideStatus;
import com.ridelink.ridemanagementservice.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FareServiceClient fareServiceClient;

    @InjectMocks
    private RideService rideService;

    private Location pickup;
    private Location destination;
    private Ride ride;

    @BeforeEach
    void setUp() {
        pickup = new Location(6.9271, 79.8612, "Fort Railway Station", "Colombo");
        destination = new Location(6.9034, 79.8553, "Bambalapitiya", "Colombo");

        ride = new Ride();
        ride.setId("ride-101");
        ride.setPassengerId("acc-pas-101");
        ride.setDriverId("drv-101");
        ride.setPickupLocation(pickup);
        ride.setDestinationLocation(destination);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setEstimatedDistanceKm(5.5);
        ride.setEstimatedDurationMinutes(15);
        ride.setEstimatedFare(850.0);
        ride.setCreatedAt(Instant.now());
        ride.setAssignedAt(Instant.now());
    }

    @Test
    void createRide_Success_AutoAssignsDriver() {
        CreateRideRequest request = new CreateRideRequest(
                "acc-pas-101", pickup, destination, 5.5, 15, true
        );

        EligibleDriverDto driverDto = new EligibleDriverDto(
                "drv-101", "acc-drv-101", "DL-12345", "Colombo", 6.9280, 79.8620, 0.5, "AVAILABLE", "ACTIVE"
        );
        FareEstimateResponseDto fareDto = new FareEstimateResponseDto(5.5, 15, 300.0, 550.0, 300.0, 1150.0);

        when(fareServiceClient.estimateFare(anyDouble(), anyInt())).thenReturn(fareDto);
        when(driverServiceClient.getEligibleDrivers(anyDouble(), anyDouble(), anyString(), anyDouble()))
                .thenReturn(List.of(driverDto));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse response = rideService.createRide(request, "acc-pas-101", "ROLE_PASSENGER");

        assertNotNull(response);
        assertEquals("ride-101", response.getId());
        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals("drv-101", response.getDriverId());
    }

    @Test
    void createRide_NoDriverAvailable_ThrowsNoDriverAvailableException() {
        // Negative Scenario 1: No available driver
        CreateRideRequest request = new CreateRideRequest(
                "acc-pas-101", pickup, destination, 5.5, 15, true
        );

        when(driverServiceClient.getEligibleDrivers(anyDouble(), anyDouble(), anyString(), anyDouble()))
                .thenReturn(Collections.emptyList());

        assertThrows(NoDriverAvailableException.class, () ->
                rideService.createRide(request, "acc-pas-101", "ROLE_PASSENGER"));
    }

    @Test
    void createRide_ForbiddenOtherUser_ThrowsForbidden() {
        CreateRideRequest request = new CreateRideRequest(
                "acc-pas-101", pickup, destination, 5.5, 15, true
        );

        assertThrows(ForbiddenException.class, () ->
                rideService.createRide(request, "acc-stranger", "ROLE_PASSENGER"));
    }

    @Test
    void acceptRide_Success() {
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.acceptRide("ride-101", "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        assertEquals(RideStatus.ACCEPTED, response.getStatus());
        assertNotNull(response.getAcceptedAt());
    }

    @Test
    void acceptRide_InvalidTransitionFromCompleted_ThrowsException() {
        // Negative Scenario 2: Invalid status transition (COMPLETED -> ACCEPTED)
        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusTransitionException.class, () ->
                rideService.acceptRide("ride-101", "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void startRide_Success() {
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.startRide("ride-101", "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        assertEquals(RideStatus.IN_PROGRESS, response.getStatus());
        assertNotNull(response.getStartedAt());
    }

    @Test
    void startRide_InvalidTransitionFromAssigned_ThrowsException() {
        // Skipping ACCEPTED state is invalid
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusTransitionException.class, () ->
                rideService.startRide("ride-101", "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void completeRide_Success_CallsFareService() {
        ride.setStatus(RideStatus.IN_PROGRESS);
        RideCompleteRequest request = new RideCompleteRequest(6.0, 18);
        FareCalculateResponseDto fareResponse = new FareCalculateResponseDto(
                "fare-101", "ride-101", 6.0, 18, 300.0, 600.0, 360.0, 1260.0, Instant.now()
        );

        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));
        when(fareServiceClient.calculateFare("ride-101", 6.0, 18)).thenReturn(fareResponse);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.completeRide("ride-101", request, "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        assertEquals(RideStatus.COMPLETED, response.getStatus());
        assertEquals(1260.0, response.getFinalFare());
        assertNotNull(response.getCompletedAt());
        verify(fareServiceClient).calculateFare("ride-101", 6.0, 18);
    }

    @Test
    void completeRide_InvalidTransitionFromAccepted_ThrowsException() {
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusTransitionException.class, () ->
                rideService.completeRide("ride-101", new RideCompleteRequest(5.0, 10), "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void cancelRide_FromRequested_Success() {
        ride.setStatus(RideStatus.REQUESTED);
        CancelRideRequest request = new CancelRideRequest("Change of schedule");

        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.cancelRide("ride-101", request, "acc-pas-101", "ROLE_PASSENGER");

        assertNotNull(response);
        assertEquals(RideStatus.CANCELLED, response.getStatus());
        assertEquals("Change of schedule", response.getCancellationReason());
    }

    @Test
    void cancelRide_FromCompleted_ThrowsInvalidTransition() {
        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusTransitionException.class, () ->
                rideService.cancelRide("ride-101", new CancelRideRequest("Too late"), "acc-pas-101", "ROLE_PASSENGER"));
    }

    @Test
    void cancelRide_FromInProgress_ThrowsInvalidTransition() {
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById("ride-101")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusTransitionException.class, () ->
                rideService.cancelRide("ride-101", new CancelRideRequest("Drop me here"), "acc-pas-101", "ROLE_PASSENGER"));
    }
}
