package com.ridelink.driver_vehicle_service.dto;

public class DriverResponse {

    private Long driverId;
    private Long userId;
    private String licenseNumber;
    private boolean available;
    private String serviceArea;
    private String currentLocation;

    public DriverResponse() {
    }

    public DriverResponse(Long driverId, Long userId, String licenseNumber,
                          boolean available, String serviceArea,
                          String currentLocation) {
        this.driverId = driverId;
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.available = available;
        this.serviceArea = serviceArea;
        this.currentLocation = currentLocation;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
}