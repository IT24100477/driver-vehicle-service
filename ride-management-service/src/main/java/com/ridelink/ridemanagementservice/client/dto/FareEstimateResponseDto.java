package com.ridelink.ridemanagementservice.client.dto;

public class FareEstimateResponseDto {

    private Double distanceKm;
    private Integer durationMinutes;
    private Double baseFare;
    private Double distanceCharge;
    private Double timeCharge;
    private Double totalFare;

    public FareEstimateResponseDto() {
    }

    public FareEstimateResponseDto(Double distanceKm, Integer durationMinutes, Double baseFare,
                                   Double distanceCharge, Double timeCharge, Double totalFare) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.totalFare = totalFare;
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

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(Double distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public Double getTimeCharge() {
        return timeCharge;
    }

    public void setTimeCharge(Double timeCharge) {
        this.timeCharge = timeCharge;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }
}
