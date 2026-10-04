package com.ridelink.ridemanagementservice.client.dto;

public class EligibleDriverDto {

    private String driverId;
    private String accountId;
    private String licenseNumber;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double distanceKm;
    private String availabilityStatus;
    private String operationalStatus;

    public EligibleDriverDto() {
    }

    public EligibleDriverDto(String driverId, String accountId, String licenseNumber, String serviceArea,
                             Double currentLatitude, Double currentLongitude, Double distanceKm,
                             String availabilityStatus, String operationalStatus) {
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

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}
