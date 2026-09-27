package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;

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
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    @Test
    void createDriver_shouldCreateDriverSuccessfully() {

        DriverRequest request = new DriverRequest();

        request.setUserId(101L);
        request.setLicenseNumber("B1234567");
        request.setAvailable(false);
        request.setServiceArea("Colombo");
        request.setCurrentLocation("Colombo 05");

        Driver savedDriver = new Driver(
                101L,
                "B1234567",
                false,
                "Colombo",
                "Colombo 05"
        );

        savedDriver.setDriverId(1L);

        when(driverRepository.save(any(Driver.class)))
                .thenReturn(savedDriver);

        DriverResponse response =
                driverService.createDriver(request);

        assertNotNull(response);
        assertEquals(1L, response.getDriverId());
        assertEquals(101L, response.getUserId());
        assertEquals("B1234567", response.getLicenseNumber());
        assertFalse(response.isAvailable());
        assertEquals("Colombo", response.getServiceArea());

        verify(driverRepository, times(1))
                .save(any(Driver.class));
    }

    @Test
    void getDriverById_shouldReturnDriverSuccessfully() {

        Driver driver = new Driver(
                101L,
                "B1234567",
                false,
                "Colombo",
                "Colombo 05"
        );

        driver.setDriverId(1L);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        DriverResponse response =
                driverService.getDriverById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getDriverId());
        assertEquals("B1234567", response.getLicenseNumber());

        verify(driverRepository, times(1))
                .findById(1L);
    }

    @Test
    void getDriverById_shouldThrowExceptionWhenDriverDoesNotExist() {

        when(driverRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.getDriverById(9999L)
        );

        verify(driverRepository, times(1))
                .findById(9999L);
    }

    @Test
    void updateAvailability_shouldUpdateDriverAvailability() {

        Driver driver = new Driver(
                101L,
                "B1234567",
                false,
                "Colombo",
                "Colombo 05"
        );

        driver.setDriverId(1L);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(driverRepository.save(any(Driver.class)))
                .thenReturn(driver);

        DriverResponse response =
                driverService.updateAvailability(1L, true);

        assertTrue(response.isAvailable());

        verify(driverRepository, times(1))
                .findById(1L);

        verify(driverRepository, times(1))
                .save(driver);
    }

    @Test
    void getAvailableDrivers_shouldReturnOnlyAvailableDrivers() {

        Driver driver1 = new Driver(
                101L,
                "B1234567",
                true,
                "Colombo",
                "Colombo 05"
        );

        driver1.setDriverId(1L);

        Driver driver2 = new Driver(
                102L,
                "B7654321",
                true,
                "Kandy",
                "Kandy City"
        );

        driver2.setDriverId(2L);

        when(driverRepository.findByAvailableTrue())
                .thenReturn(List.of(driver1, driver2));

        List<DriverResponse> responses =
                driverService.getAvailableDrivers();

        assertEquals(2, responses.size());

        assertTrue(responses.get(0).isAvailable());
        assertTrue(responses.get(1).isAvailable());

        verify(driverRepository, times(1))
                .findByAvailableTrue();
    }
}