package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.model.Vehicle;

import java.time.Instant;

public class VehicleResponse {

    private String id;
    private String driverId;
    private String registrationNumber;
    private String make;
    private String model;
    private String color;
    private String vehicleType;
    private Integer capacity;
    private Instant createdAt;
    private Instant updatedAt;

    public VehicleResponse() {
    }

    public VehicleResponse(String id, String driverId, String registrationNumber, String make, String model,
                           String color, String vehicleType, Integer capacity, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.color = color;
        this.vehicleType = vehicleType;
        this.capacity = capacity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDriverId(),
                vehicle.getRegistrationNumber(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getVehicleType(),
                vehicle.getCapacity(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
