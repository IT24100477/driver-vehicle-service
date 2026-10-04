package com.ridelink.driverservice.controller;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.OperationalStatus;
import com.ridelink.driverservice.service.DriverService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DriverController driverController;

    @Test
    void createDriver_ReturnsCreated() {
        DriverCreateRequest request = new DriverCreateRequest("acc-drv-101", "DL-12345", "Colombo", 6.9271, 79.8612);
        DriverResponse response = new DriverResponse(
                "drv-101", "acc-drv-101", "DL-12345", AvailabilityStatus.OFFLINE, "Colombo", 6.9271, 79.8612,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        when(driverService.createDriver(eq(request), eq("acc-drv-101"), eq("ROLE_DRIVER")))
                .thenReturn(response);

        ResponseEntity<DriverResponse> result = driverController.createDriver(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("drv-101", result.getBody().getId());
    }

    @Test
    void getAvailableDrivers_ReturnsList() {
        DriverResponse response = new DriverResponse(
                "drv-101", "acc-drv-101", "DL-12345", AvailabilityStatus.AVAILABLE, "Colombo", 6.9271, 79.8612,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        when(driverService.getAvailableDrivers()).thenReturn(List.of(response));

        ResponseEntity<List<DriverResponse>> result = driverController.getAvailableDrivers();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getEligibleDrivers_ReturnsList() {
        EligibleDriverResponse eligible = new EligibleDriverResponse(
                "drv-101", "acc-drv-101", "DL-12345", "Colombo", 6.9271, 79.8612, 1.25,
                AvailabilityStatus.AVAILABLE, OperationalStatus.ACTIVE
        );

        when(driverService.getEligibleDrivers(eq(6.9271), eq(79.8612), isNull(), eq(15.0)))
                .thenReturn(List.of(eligible));

        ResponseEntity<List<EligibleDriverResponse>> result = driverController.getEligibleDrivers(6.9271, 79.8612, null, 15.0);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(1.25, result.getBody().get(0).getDistanceKm());
    }
}
