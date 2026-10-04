package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.Driver;
import com.ridelink.driverservice.model.OperationalStatus;

import java.time.Instant;

public class DriverResponse {

    private String id;
    private String accountId;
    private String licenseNumber;
    private AvailabilityStatus availabilityStatus;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
    private OperationalStatus operationalStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public DriverResponse() {
    }

    public DriverResponse(String id, String accountId, String licenseNumber, AvailabilityStatus availabilityStatus,
                          String serviceArea, Double currentLatitude, Double currentLongitude,
                          OperationalStatus operationalStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.licenseNumber = licenseNumber;
        this.availabilityStatus = availabilityStatus;
        this.serviceArea = serviceArea;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.operationalStatus = operationalStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DriverResponse fromEntity(Driver driver) {
        if (driver == null) {
            return null;
        }
        return new DriverResponse(
                driver.getId(),
                driver.getAccountId(),
                driver.getLicenseNumber(),
                driver.getAvailabilityStatus(),
                driver.getServiceArea(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                driver.getOperationalStatus(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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

    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(OperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
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
