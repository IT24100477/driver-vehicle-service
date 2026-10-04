package com.ridelink.driverservice.service;

import com.ridelink.driverservice.dto.VehicleRequest;
import com.ridelink.driverservice.dto.VehicleResponse;
import com.ridelink.driverservice.exception.ConflictException;
import com.ridelink.driverservice.exception.ForbiddenException;
import com.ridelink.driverservice.exception.ResourceNotFoundException;
import com.ridelink.driverservice.model.Driver;
import com.ridelink.driverservice.model.Vehicle;
import com.ridelink.driverservice.repository.DriverRepository;
import com.ridelink.driverservice.repository.VehicleRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Driver driver;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId("drv-101");
        driver.setAccountId("acc-drv-101");

        vehicle = new Vehicle();
        vehicle.setId("veh-101");
        vehicle.setDriverId("drv-101");
        vehicle.setRegistrationNumber("WP-CAR-1234");
        vehicle.setMake("Toyota");
        vehicle.setModel("Prius");
        vehicle.setColor("White");
        vehicle.setVehicleType("SEDAN");
        vehicle.setCapacity(4);
        vehicle.setCreatedAt(Instant.now());
        vehicle.setUpdatedAt(Instant.now());
    }

    @Test
    void createVehicle_Success() {
        VehicleRequest request = new VehicleRequest(
                "drv-101", "WP-CAR-1234", "Toyota", "Prius", "White", "SEDAN", 4
        );

        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));
        when(vehicleRepository.existsByRegistrationNumber("WP-CAR-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.createVehicle(request, "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        assertEquals("veh-101", response.getId());
        assertEquals("WP-CAR-1234", response.getRegistrationNumber());
    }

    @Test
    void createVehicle_DriverNotFound_ThrowsResourceNotFound() {
        VehicleRequest request = new VehicleRequest(
                "drv-unknown", "WP-CAR-1234", "Toyota", "Prius", "White", "SEDAN", 4
        );

        when(driverRepository.findById("drv-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                vehicleService.createVehicle(request, "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void createVehicle_ForbiddenOtherUser_ThrowsForbidden() {
        VehicleRequest request = new VehicleRequest(
                "drv-101", "WP-CAR-1234", "Toyota", "Prius", "White", "SEDAN", 4
        );

        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));

        assertThrows(ForbiddenException.class, () ->
                vehicleService.createVehicle(request, "acc-stranger", "ROLE_DRIVER"));
    }

    @Test
    void createVehicle_DuplicateRegistration_ThrowsConflict() {
        VehicleRequest request = new VehicleRequest(
                "drv-101", "WP-CAR-1234", "Toyota", "Prius", "White", "SEDAN", 4
        );

        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));
        when(vehicleRepository.existsByRegistrationNumber("WP-CAR-1234")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                vehicleService.createVehicle(request, "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById("veh-101")).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById("veh-101");

        assertNotNull(response);
        assertEquals("veh-101", response.getId());
    }

    @Test
    void getVehiclesByDriverId_ReturnsList() {
        when(vehicleRepository.findByDriverId("drv-101")).thenReturn(Collections.singletonList(vehicle));

        List<VehicleResponse> vehicles = vehicleService.getVehiclesByDriverId("drv-101");

        assertEquals(1, vehicles.size());
        assertEquals("veh-101", vehicles.get(0).getId());
    }

    @Test
    void deleteVehicle_Success() {
        when(vehicleRepository.findById("veh-101")).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));

        assertDoesNotThrow(() -> vehicleService.deleteVehicle("veh-101", "acc-drv-101", "ROLE_DRIVER"));

        verify(vehicleRepository, times(1)).deleteById("veh-101");
    }
}
