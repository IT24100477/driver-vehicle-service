package com.ridelink.driverservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotBlank(message = "Registration number is required")
    @Size(min = 3, max = 20, message = "Registration number must be between 3 and 20 characters")
    private String registrationNumber;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Color is required")
    private String color;

    @NotBlank(message = "Vehicle type is required (e.g., SEDAN, SUV, VAN)")
    private String vehicleType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 20, message = "Capacity cannot exceed 20")
    private Integer capacity;

    public VehicleRequest() {
    }

    public VehicleRequest(String driverId, String registrationNumber, String make, String model,
                          String color, String vehicleType, Integer capacity) {
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.color = color;
        this.vehicleType = vehicleType;
        this.capacity = capacity;
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
}
