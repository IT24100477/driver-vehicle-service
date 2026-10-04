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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public VehicleResponse createVehicle(VehicleRequest request, String requestingAccountId, String requestingRole) {
        log.info("Creating vehicle {} for driverId: {}", request.getRegistrationNumber(), request.getDriverId());

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + request.getDriverId()));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber().trim())) {
            throw new ConflictException("Vehicle with registration number already exists: " + request.getRegistrationNumber());
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId(request.getDriverId());
        vehicle.setRegistrationNumber(request.getRegistrationNumber().trim().toUpperCase());
        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setColor(request.getColor().trim());
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        vehicle.setCapacity(request.getCapacity());
        Instant now = Instant.now();
        vehicle.setCreatedAt(now);
        vehicle.setUpdatedAt(now);

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", saved.getId());
        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse getVehicleById(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
        return VehicleResponse.fromEntity(vehicle);
    }

    public List<VehicleResponse> getVehiclesByDriverId(String driverId) {
        return vehicleRepository.findByDriverId(driverId)
                .stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VehicleResponse updateVehicle(String id, VehicleRequest request, String requestingAccountId, String requestingRole) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        Driver driver = driverRepository.findById(vehicle.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated driver not found: " + vehicle.getDriverId()));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        String regNum = request.getRegistrationNumber().trim().toUpperCase();
        if (!vehicle.getRegistrationNumber().equalsIgnoreCase(regNum)
                && vehicleRepository.existsByRegistrationNumber(regNum)) {
            throw new ConflictException("Vehicle with registration number already exists: " + regNum);
        }

        vehicle.setRegistrationNumber(regNum);
        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setColor(request.getColor().trim());
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        vehicle.setCapacity(request.getCapacity());
        vehicle.setUpdatedAt(Instant.now());

        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(updated);
    }

    public void deleteVehicle(String id, String requestingAccountId, String requestingRole) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        Driver driver = driverRepository.findById(vehicle.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated driver not found: " + vehicle.getDriverId()));

        verifyDriverOwnership(driver, requestingAccountId, requestingRole);

        vehicleRepository.deleteById(id);
        log.info("Vehicle ID: {} deleted successfully", id);
    }

    private void verifyDriverOwnership(Driver driver, String requestingAccountId, String requestingRole) {
        boolean isAdmin = "ROLE_ADMIN".equals(requestingRole);
        boolean isOwner = driver.getAccountId() != null && driver.getAccountId().equals(requestingAccountId);

        if (!isAdmin && !isOwner) {
            log.warn("Unauthorized vehicle operation by accountId: {} on driverId: {}", requestingAccountId, driver.getId());
            throw new ForbiddenException("Access denied: You do not own this driver operational profile");
        }
    }
}
