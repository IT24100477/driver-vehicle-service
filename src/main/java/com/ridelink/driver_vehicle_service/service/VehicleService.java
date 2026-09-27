package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.VehicleRequest;
import com.ridelink.driver_vehicle_service.dto.VehicleResponse;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.entity.Vehicle;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    // Add a new vehicle
    public VehicleResponse createVehicle(VehicleRequest request) {

        // Check whether the driver exists
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + request.getDriverId()));

        Vehicle vehicle = new Vehicle(
                driver.getDriverId(),
                request.getRegistrationNumber(),
                request.getVehicleType(),
                request.getModel(),
                request.getColour()
        );

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponse(savedVehicle);
    }

    // Get a vehicle by ID
    public VehicleResponse getVehicleById(Long vehicleId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + vehicleId));

        return convertToResponse(vehicle);
    }

    // Update vehicle details
    public VehicleResponse updateVehicle(Long vehicleId,
                                         VehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + vehicleId));

        // Check whether the new driver exists
        driverRepository.findById(request.getDriverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: "
                                        + request.getDriverId()));

        vehicle.setDriverId(request.getDriverId());
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setModel(request.getModel());
        vehicle.setColour(request.getColour());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        return convertToResponse(updatedVehicle);
    }

    // Get all vehicles belonging to a driver
    public List<VehicleResponse> getVehiclesByDriverId(Long driverId) {

        // Check whether the driver exists
        driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + driverId));

        return vehicleRepository.findByDriverId(driverId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get all vehicles
    public List<VehicleResponse> getAllVehicles() {

        return vehicleRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Convert Vehicle entity to VehicleResponse DTO
    private VehicleResponse convertToResponse(Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getVehicleId(),
                vehicle.getDriverId(),
                vehicle.getRegistrationNumber(),
                vehicle.getVehicleType(),
                vehicle.getModel(),
                vehicle.getColour()
        );
    }
}