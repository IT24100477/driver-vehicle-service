package com.ridelink.driverservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DriverUpdateRequest {

    @NotBlank(message = "License number is required")
    @Size(min = 5, max = 30, message = "License number must be between 5 and 30 characters")
    private String licenseNumber;

    @NotBlank(message = "Service area is required")
    private String serviceArea;

    public DriverUpdateRequest() {
    }

    public DriverUpdateRequest(String licenseNumber, String serviceArea) {
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
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
}
