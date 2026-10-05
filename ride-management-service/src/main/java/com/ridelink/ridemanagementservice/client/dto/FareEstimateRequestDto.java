package com.ridelink.ridemanagementservice.client.dto;

public class FareEstimateRequestDto {

    private Double distanceKm;
    private Integer durationMinutes;

    public FareEstimateRequestDto() {
    }

    public FareEstimateRequestDto(Double distanceKm, Integer durationMinutes) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
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
