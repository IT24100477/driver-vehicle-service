package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.OperationalStatus;

public class EligibleDriverResponse {

    private String driverId;
    private String accountId;
    private String licenseNumber;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double distanceKm;
    private AvailabilityStatus availabilityStatus;
    private OperationalStatus operationalStatus;

    public EligibleDriverResponse() {
    }

    public EligibleDriverResponse(String driverId, String accountId, String licenseNumber, String serviceArea,
                                  Double currentLatitude, Double currentLongitude, Double distanceKm,
                                  AvailabilityStatus availabilityStatus, OperationalStatus operationalStatus) {
        this.driverId = driverId;
        this.accountId = accountId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.distanceKm = distanceKm;
        this.availabilityStatus = availabilityStatus;
        this.operationalStatus = operationalStatus;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(OperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}
