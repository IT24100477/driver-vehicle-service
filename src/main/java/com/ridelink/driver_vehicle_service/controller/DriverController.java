package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.service.DriverService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // Create a new driver
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody DriverRequest request) {

        DriverResponse response = driverService.createDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get a driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable Long id) {

        DriverResponse response = driverService.getDriverById(id);

        return ResponseEntity.ok(response);
    }

    // Get all drivers
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {

        return ResponseEntity.ok(
                driverService.getAllDrivers()
        );
    }

    // Update driver details
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long id,
            @Valid @RequestBody DriverRequest request) {

        DriverResponse response =
                driverService.updateDriver(id, request);

        return ResponseEntity.ok(response);
    }

    // Update driver availability
    @PatchMapping("/{id}/availability")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {

        DriverResponse response =
                driverService.updateAvailability(id, available);

        return ResponseEntity.ok(response);
    }

    // Update driver's current location
    @PatchMapping("/{id}/location")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable Long id,
            @RequestParam String currentLocation) {

        DriverResponse response =
                driverService.updateLocation(id, currentLocation);

        return ResponseEntity.ok(response);
    }

    // Update driver's service area
    @PatchMapping("/{id}/service-area")
    public ResponseEntity<DriverResponse> updateServiceArea(
            @PathVariable Long id,
            @RequestParam String serviceArea) {

        DriverResponse response =
                driverService.updateServiceArea(id, serviceArea);

        return ResponseEntity.ok(response);
    }

    // Get available drivers
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {

        return ResponseEntity.ok(
                driverService.getAvailableDrivers()
        );
    }
}