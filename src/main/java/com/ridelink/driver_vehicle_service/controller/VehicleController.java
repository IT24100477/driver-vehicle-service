package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.dto.VehicleRequest;
import com.ridelink.driver_vehicle_service.dto.VehicleResponse;
import com.ridelink.driver_vehicle_service.service.VehicleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // Add a new vehicle
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody VehicleRequest request) {

        VehicleResponse response =
                vehicleService.createVehicle(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get a vehicle by ID
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(
            @PathVariable Long id) {

        VehicleResponse response =
                vehicleService.getVehicleById(id);

        return ResponseEntity.ok(response);
    }

    // Update vehicle details
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {

        VehicleResponse response =
                vehicleService.updateVehicle(id, request);

        return ResponseEntity.ok(response);
    }

    // Get all vehicles belonging to a driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByDriverId(
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                vehicleService.getVehiclesByDriverId(driverId)
        );
    }

    // Get all vehicles
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {

        return ResponseEntity.ok(
                vehicleService.getAllVehicles()
        );
    }
}