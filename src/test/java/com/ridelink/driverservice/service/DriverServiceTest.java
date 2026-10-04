package com.ridelink.driverservice.service;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.exception.ConflictException;
import com.ridelink.driverservice.exception.ForbiddenException;
import com.ridelink.driverservice.exception.ResourceNotFoundException;
import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.Driver;
import com.ridelink.driverservice.model.OperationalStatus;
import com.ridelink.driverservice.repository.DriverRepository;
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
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId("drv-101");
        driver.setAccountId("acc-drv-101");
        driver.setLicenseNumber("DL-98765432");
        driver.setServiceArea("Colombo");
        driver.setCurrentLatitude(6.9271);
        driver.setCurrentLongitude(79.8612);
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        driver.setOperationalStatus(OperationalStatus.ACTIVE);
        driver.setCreatedAt(Instant.now());
        driver.setUpdatedAt(Instant.now());
    }

    @Test
    void createDriver_Success() {
        DriverCreateRequest request = new DriverCreateRequest(
                "acc-drv-101", "DL-98765432", "Colombo", 6.9271, 79.8612
        );

        when(driverRepository.existsByAccountId("acc-drv-101")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("DL-98765432")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.createDriver(request, "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        assertEquals("drv-101", response.getId());
        assertEquals("DL-98765432", response.getLicenseNumber());
        assertEquals("Colombo", response.getServiceArea());
    }

    @Test
    void createDriver_DuplicateAccount_ThrowsConflict() {
        DriverCreateRequest request = new DriverCreateRequest(
                "acc-drv-101", "DL-98765432", "Colombo", 6.9271, 79.8612
        );

        when(driverRepository.existsByAccountId("acc-drv-101")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                driverService.createDriver(request, "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void createDriver_DuplicateLicense_ThrowsConflict() {
        DriverCreateRequest request = new DriverCreateRequest(
                "acc-drv-101", "DL-98765432", "Colombo", 6.9271, 79.8612
        );

        when(driverRepository.existsByAccountId("acc-drv-101")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("DL-98765432")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                driverService.createDriver(request, "acc-drv-101", "ROLE_DRIVER"));
    }

    @Test
    void createDriver_ForbiddenOtherUser_ThrowsForbidden() {
        DriverCreateRequest request = new DriverCreateRequest(
                "acc-drv-101", "DL-98765432", "Colombo", 6.9271, 79.8612
        );

        assertThrows(ForbiddenException.class, () ->
                driverService.createDriver(request, "acc-other-user", "ROLE_DRIVER"));
    }

    @Test
    void getDriverById_Success() {
        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));

        DriverResponse response = driverService.getDriverById("drv-101");

        assertNotNull(response);
        assertEquals("drv-101", response.getId());
    }

    @Test
    void getDriverById_NotFound_ThrowsException() {
        when(driverRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                driverService.getDriverById("non-existent"));
    }

    @Test
    void updateAvailability_Success() {
        DriverAvailabilityUpdateRequest updateReq = new DriverAvailabilityUpdateRequest(AvailabilityStatus.BUSY);

        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.updateAvailability("drv-101", updateReq, "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void updateLocation_Success() {
        DriverLocationUpdateRequest updateReq = new DriverLocationUpdateRequest(6.9319, 79.8478);

        when(driverRepository.findById("drv-101")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.updateLocation("drv-101", updateReq, "acc-drv-101", "ROLE_DRIVER");

        assertNotNull(response);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void getAvailableDrivers_ReturnsList() {
        when(driverRepository.findByOperationalStatusAndAvailabilityStatus(
                OperationalStatus.ACTIVE, AvailabilityStatus.AVAILABLE))
                .thenReturn(Collections.singletonList(driver));

        List<DriverResponse> available = driverService.getAvailableDrivers();

        assertEquals(1, available.size());
        assertEquals("drv-101", available.get(0).getId());
    }

    @Test
    void getEligibleDrivers_WithinRadius_ReturnsSorted() {
        Driver driverFar = new Driver();
        driverFar.setId("drv-far");
        driverFar.setAccountId("acc-far");
        driverFar.setLicenseNumber("DL-FAR123");
        driverFar.setServiceArea("Kandy");
        driverFar.setCurrentLatitude(7.2906); // Kandy ~115km away
        driverFar.setCurrentLongitude(80.6337);
        driverFar.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        driverFar.setOperationalStatus(OperationalStatus.ACTIVE);

        when(driverRepository.findByOperationalStatusAndAvailabilityStatus(
                OperationalStatus.ACTIVE, AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(driver, driverFar));

        List<EligibleDriverResponse> eligible = driverService.getEligibleDrivers(
                6.9271, 79.8612, null, 15.0);

        // Near driver should match, far driver in Kandy should be excluded by 15km radius
        assertEquals(1, eligible.size());
        assertEquals("drv-101", eligible.get(0).getDriverId());
        assertTrue(eligible.get(0).getDistanceKm() <= 1.0);
    }
}
