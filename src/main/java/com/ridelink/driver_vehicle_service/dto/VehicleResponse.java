package com.ridelink.driver_vehicle_service.dto;

public class VehicleResponse {

    private Long vehicleId;
    private Long driverId;
    private String registrationNumber;
    private String vehicleType;
    private String model;
    private String colour;

    public VehicleResponse() {
    }

    public VehicleResponse(Long vehicleId, Long driverId,
                           String registrationNumber, String vehicleType,
                           String model, String colour) {
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
        this.model = model;
        this.colour = colour;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}