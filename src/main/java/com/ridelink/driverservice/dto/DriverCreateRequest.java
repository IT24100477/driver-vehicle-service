package com.ridelink.driverservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DriverCreateRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "License number is required")
    @Size(min = 5, max = 30, message = "License number must be between 5 and 30 characters")
    private String licenseNumber;

    @NotBlank(message = "Service area is required")
    private String serviceArea;

    @NotNull(message = "Current latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private Double currentLatitude;

    @NotNull(message = "Current longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private Double currentLongitude;

    public DriverCreateRequest() {
    }

    public DriverCreateRequest(String accountId, String licenseNumber, String serviceArea,
                               Double currentLatitude, Double currentLongitude) {
        this.accountId = accountId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }
}
