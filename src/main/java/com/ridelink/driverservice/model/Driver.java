package com.ridelink.driverservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    @Indexed
    private String accountId;

    @Indexed(unique = true)
    private String licenseNumber;

    @Indexed
    private AvailabilityStatus availabilityStatus;

    private String serviceArea;

    private Double currentLatitude;

    private Double currentLongitude;

    @Indexed
    private OperationalStatus operationalStatus;

    private Instant createdAt;

    private Instant updatedAt;

    public Driver() {
    }

    public Driver(String id, String accountId, String licenseNumber, AvailabilityStatus availabilityStatus,
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
