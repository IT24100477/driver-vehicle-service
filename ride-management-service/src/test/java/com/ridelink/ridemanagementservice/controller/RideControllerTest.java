package com.ridelink.ridemanagementservice.controller;

import com.ridelink.ridemanagementservice.dto.*;
import com.ridelink.ridemanagementservice.model.Location;
import com.ridelink.ridemanagementservice.model.RideStatus;
import com.ridelink.ridemanagementservice.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

    @Mock
    private RideService rideService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RideController rideController;

    private RideResponse sampleResponse;

    @BeforeEach
    void setUp() {
        Location pickup = new Location(6.9271, 79.8612, "Fort", "Colombo");
        Location destination = new Location(6.9034, 79.8553, "Bambalapitiya", "Colombo");

        sampleResponse = new RideResponse(
                "ride-101", "acc-pas-101", "drv-101", pickup, destination,
                RideStatus.ASSIGNED, 5.0, 15, 800.0, null, null,
                Instant.now(), Instant.now(), null, null, null, null
        );
    }

    @Test
    void createRide_ReturnsCreated() {
        CreateRideRequest request = new CreateRideRequest("acc-pas-101", sampleResponse.getPickupLocation(),
                sampleResponse.getDestinationLocation(), 5.0, 15, true);

        when(authentication.getPrincipal()).thenReturn("acc-pas-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .when(authentication).getAuthorities();
        when(rideService.createRide(eq(request), eq("acc-pas-101"), eq("ROLE_PASSENGER")))
                .thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.createRide(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("ride-101", result.getBody().getId());
    }

    @Test
    void getRideById_ReturnsOk() {
        when(rideService.getRideById("ride-101")).thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.getRideById("ride-101");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("ride-101", result.getBody().getId());
    }

    @Test
    void acceptRide_ReturnsOk() {
        sampleResponse.setStatus(RideStatus.ACCEPTED);
        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        when(rideService.acceptRide(eq("ride-101"), eq("acc-drv-101"), eq("ROLE_DRIVER")))
                .thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.acceptRide("ride-101", authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(RideStatus.ACCEPTED, result.getBody().getStatus());
    }

    @Test
    void startRide_ReturnsOk() {
        sampleResponse.setStatus(RideStatus.IN_PROGRESS);
        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        when(rideService.startRide(eq("ride-101"), eq("acc-drv-101"), eq("ROLE_DRIVER")))
                .thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.startRide("ride-101", authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(RideStatus.IN_PROGRESS, result.getBody().getStatus());
    }

    @Test
    void completeRide_ReturnsOk() {
        sampleResponse.setStatus(RideStatus.COMPLETED);
        sampleResponse.setFinalFare(1200.0);
        RideCompleteRequest request = new RideCompleteRequest(5.0, 15);

        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        when(rideService.completeRide(eq("ride-101"), eq(request), eq("acc-drv-101"), eq("ROLE_DRIVER")))
                .thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.completeRide("ride-101", request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(RideStatus.COMPLETED, result.getBody().getStatus());
        assertEquals(1200.0, result.getBody().getFinalFare());
    }

    @Test
    void cancelRide_ReturnsOk() {
        sampleResponse.setStatus(RideStatus.CANCELLED);
        sampleResponse.setCancellationReason("Trip cancelled");
        CancelRideRequest request = new CancelRideRequest("Trip cancelled");

        when(authentication.getPrincipal()).thenReturn("acc-pas-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .when(authentication).getAuthorities();
        when(rideService.cancelRide(eq("ride-101"), eq(request), eq("acc-pas-101"), eq("ROLE_PASSENGER")))
                .thenReturn(sampleResponse);

        ResponseEntity<RideResponse> result = rideController.cancelRide("ride-101", request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(RideStatus.CANCELLED, result.getBody().getStatus());
    }
}
