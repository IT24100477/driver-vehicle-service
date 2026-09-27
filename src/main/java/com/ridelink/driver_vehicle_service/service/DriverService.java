package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // Create a new driver
    public DriverResponse createDriver(DriverRequest request) {

        Driver driver = new Driver(
                request.getUserId(),
                request.getLicenseNumber(),
                false,
                request.getServiceArea(),
                request.getCurrentLocation()
        );

        Driver savedDriver = driverRepository.save(driver);

        return convertToResponse(savedDriver);
    }

    // Get a driver by ID
    public DriverResponse getDriverById(Long driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with ID: " + driverId));

        return convertToResponse(driver);
    }

    // Get all drivers
    public List<DriverResponse> getAllDrivers() {

        return driverRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Update driver details
    public DriverResponse updateDriver(Long driverId, DriverRequest request) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with ID: " + driverId));

        driver.setUserId(request.getUserId());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setServiceArea(request.getServiceArea());
        driver.setCurrentLocation(request.getCurrentLocation());

        Driver updatedDriver = driverRepository.save(driver);

        return convertToResponse(updatedDriver);
    }

    // Update driver availability
    public DriverResponse updateAvailability(Long driverId, boolean available) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with ID: " + driverId));

        driver.setAvailable(available);

        Driver updatedDriver = driverRepository.save(driver);

        return convertToResponse(updatedDriver);
    }

    // Update driver's current location
    public DriverResponse updateLocation(Long driverId, String currentLocation) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with ID: " + driverId));

        driver.setCurrentLocation(currentLocation);

        Driver updatedDriver = driverRepository.save(driver);

        return convertToResponse(updatedDriver);
    }

    // Update driver's service area
    public DriverResponse updateServiceArea(Long driverId, String serviceArea) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with ID: " + driverId));

        driver.setServiceArea(serviceArea);

        Driver updatedDriver = driverRepository.save(driver);

        return convertToResponse(updatedDriver);
    }

    // Get only available drivers
    public List<DriverResponse> getAvailableDrivers() {

        return driverRepository.findByAvailableTrue()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Convert Driver entity to DriverResponse DTO
    private DriverResponse convertToResponse(Driver driver) {

        return new DriverResponse(
                driver.getDriverId(),
                driver.getUserId(),
                driver.getLicenseNumber(),
                driver.isAvailable(),
                driver.getServiceArea(),
                driver.getCurrentLocation()
        );
    }
}