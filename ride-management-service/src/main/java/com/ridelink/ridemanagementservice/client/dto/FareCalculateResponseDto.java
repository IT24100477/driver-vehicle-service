package com.ridelink.ridemanagementservice.client.dto;

import java.time.Instant;

public class FareCalculateResponseDto {

    private String id;
    private String rideId;
    private Double distanceKm;
    private Integer durationMinutes;
    private Double baseFare;
    private Double distanceCharge;
    private Double timeCharge;
    private Double totalFare;
    private Instant createdAt;

    public FareCalculateResponseDto() {
    }

    public FareCalculateResponseDto(String id, String rideId, Double distanceKm, Integer durationMinutes,
                                   Double baseFare, Double distanceCharge, Double timeCharge, Double totalFare, Instant createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.totalFare = totalFare;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
