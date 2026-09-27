package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.VehicleRequest;
import com.ridelink.driver_vehicle_service.dto.VehicleResponse;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.entity.Vehicle;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createVehicle_shouldCreateVehicleSuccessfully() {

        VehicleRequest request = new VehicleRequest();

        request.setDriverId(1L);
        request.setRegistrationNumber("CAB-1234");
        request.setVehicleType("Car");
        request.setModel("Toyota Prius");
        request.setColour("White");

        Driver driver = new Driver(
                101L,
                "B1234567",
                true,
                "Colombo",
                "Colombo 05"
        );

        driver.setDriverId(1L);

        Vehicle savedVehicle = new Vehicle(
                1L,
                "CAB-1234",
                "Car",
                "Toyota Prius",
                "White"
        );

        savedVehicle.setVehicleId(10L);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        VehicleResponse response =
                vehicleService.createVehicle(request);

        assertNotNull(response);
        assertEquals(10L, response.getVehicleId());
        assertEquals(1L, response.getDriverId());
        assertEquals("CAB-1234", response.getRegistrationNumber());
        assertEquals("Car", response.getVehicleType());
        assertEquals("Toyota Prius", response.getModel());
        assertEquals("White", response.getColour());

        verify(driverRepository, times(1))
                .findById(1L);

        verify(vehicleRepository, times(1))
                .save(any(Vehicle.class));
    }

    @Test
    void createVehicle_shouldThrowExceptionWhenDriverDoesNotExist() {

        VehicleRequest request = new VehicleRequest();

        request.setDriverId(9999L);
        request.setRegistrationNumber("CAB-9999");
        request.setVehicleType("Car");
        request.setModel("Toyota Prius");
        request.setColour("White");

        when(driverRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehicleService.createVehicle(request)
        );

        verify(driverRepository, times(1))
                .findById(9999L);

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    @Test
    void getVehicleById_shouldReturnVehicleSuccessfully() {

        Vehicle vehicle = new Vehicle(
                1L,
                "CAB-1234",
                "Car",
                "Toyota Prius",
                "White"
        );

        vehicle.setVehicleId(10L);

        when(vehicleRepository.findById(10L))
                .thenReturn(Optional.of(vehicle));

        VehicleResponse response =
                vehicleService.getVehicleById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getVehicleId());
        assertEquals(1L, response.getDriverId());
        assertEquals("CAB-1234", response.getRegistrationNumber());

        verify(vehicleRepository, times(1))
                .findById(10L);
    }

    @Test
    void getVehicleById_shouldThrowExceptionWhenVehicleDoesNotExist() {

        when(vehicleRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehicleService.getVehicleById(9999L)
        );

        verify(vehicleRepository, times(1))
                .findById(9999L);
    }

    @Test
    void updateVehicle_shouldUpdateVehicleSuccessfully() {

        VehicleRequest request = new VehicleRequest();

        request.setDriverId(1L);
        request.setRegistrationNumber("CAB-5678");
        request.setVehicleType("Car");
        request.setModel("Honda Vezel");
        request.setColour("Black");

        Vehicle existingVehicle = new Vehicle(
                1L,
                "CAB-1234",
                "Car",
                "Toyota Prius",
                "White"
        );

        existingVehicle.setVehicleId(10L);

        Driver driver = new Driver(
                101L,
                "B1234567",
                true,
                "Colombo",
                "Colombo 05"
        );

        driver.setDriverId(1L);

        when(vehicleRepository.findById(10L))
                .thenReturn(Optional.of(existingVehicle));

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(existingVehicle);

        VehicleResponse response =
                vehicleService.updateVehicle(10L, request);

        assertEquals("CAB-5678",
                response.getRegistrationNumber());

        assertEquals("Honda Vezel",
                response.getModel());

        assertEquals("Black",
                response.getColour());

        verify(vehicleRepository, times(1))
                .findById(10L);

        verify(driverRepository, times(1))
                .findById(1L);

        verify(vehicleRepository, times(1))
                .save(existingVehicle);
    }

    @Test
    void getVehiclesByDriverId_shouldReturnVehiclesSuccessfully() {

        Driver driver = new Driver(
                101L,
                "B1234567",
                true,
                "Colombo",
                "Colombo 05"
        );

        driver.setDriverId(1L);

        Vehicle vehicle1 = new Vehicle(
                1L,
                "CAB-1234",
                "Car",
                "Toyota Prius",
                "White"
        );

        vehicle1.setVehicleId(10L);

        Vehicle vehicle2 = new Vehicle(
                1L,
                "CAB-5678",
                "Car",
                "Honda Vezel",
                "Black"
        );

        vehicle2.setVehicleId(11L);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findByDriverId(1L))
                .thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleResponse> responses =
                vehicleService.getVehiclesByDriverId(1L);

        assertEquals(2, responses.size());

        assertEquals("CAB-1234",
                responses.get(0).getRegistrationNumber());

        assertEquals("CAB-5678",
                responses.get(1).getRegistrationNumber());

        verify(driverRepository, times(1))
                .findById(1L);

        verify(vehicleRepository, times(1))
                .findByDriverId(1L);
    }
}