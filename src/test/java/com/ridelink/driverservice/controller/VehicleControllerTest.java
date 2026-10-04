package com.ridelink.driverservice.controller;

import com.ridelink.driverservice.dto.VehicleRequest;
import com.ridelink.driverservice.dto.VehicleResponse;
import com.ridelink.driverservice.service.VehicleService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VehicleController vehicleController;

    @Test
    void createVehicle_ReturnsCreated() {
        VehicleRequest request = new VehicleRequest("drv-101", "WP-CAR-1111", "Toyota", "Axio", "Silver", "SEDAN", 4);
        VehicleResponse response = new VehicleResponse(
                "veh-101", "drv-101", "WP-CAR-1111", "Toyota", "Axio", "Silver", "SEDAN", 4,
                Instant.now(), Instant.now()
        );

        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        when(vehicleService.createVehicle(eq(request), eq("acc-drv-101"), eq("ROLE_DRIVER")))
                .thenReturn(response);

        ResponseEntity<VehicleResponse> result = vehicleController.createVehicle(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("veh-101", result.getBody().getId());
    }

    @Test
    void getVehicleById_ReturnsVehicle() {
        VehicleResponse response = new VehicleResponse(
                "veh-101", "drv-101", "WP-CAR-1111", "Toyota", "Axio", "Silver", "SEDAN", 4,
                Instant.now(), Instant.now()
        );

        when(vehicleService.getVehicleById("veh-101")).thenReturn(response);

        ResponseEntity<VehicleResponse> result = vehicleController.getVehicleById("veh-101");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("veh-101", result.getBody().getId());
    }

    @Test
    void deleteVehicle_ReturnsNoContent() {
        when(authentication.getPrincipal()).thenReturn("acc-drv-101");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_DRIVER")))
                .when(authentication).getAuthorities();
        doNothing().when(vehicleService).deleteVehicle("veh-101", "acc-drv-101", "ROLE_DRIVER");

        ResponseEntity<Void> result = vehicleController.deleteVehicle("veh-101", authentication);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(vehicleService).deleteVehicle("veh-101", "acc-drv-101", "ROLE_DRIVER");
    }
}
