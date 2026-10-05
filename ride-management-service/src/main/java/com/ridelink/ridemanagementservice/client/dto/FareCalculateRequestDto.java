package com.ridelink.ridemanagementservice.client.dto;

public class FareCalculateRequestDto {

    private String rideId;
    private Double distanceKm;
    private Integer durationMinutes;

    public FareCalculateRequestDto() {
    }

    public FareCalculateRequestDto(String rideId, Double distanceKm, Integer durationMinutes) {
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
