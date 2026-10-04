package com.ridelink.ridemanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request body when driver completes a ride trip")
public class RideCompleteRequest {

    @NotNull(message = "Actual distance in km is required")
    @Positive(message = "Actual distance must be positive")
    @Schema(description = "Actual trip distance in kilometers", example = "9.5")
    private Double actualDistanceKm;

    @NotNull(message = "Actual duration in minutes is required")
    @PositiveOrZero(message = "Actual duration cannot be negative")
    @Schema(description = "Actual trip duration in minutes", example = "22")
    private Integer actualDurationMinutes;

    public RideCompleteRequest() {
    }

    public RideCompleteRequest(Double actualDistanceKm, Integer actualDurationMinutes) {
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public void setActualDistanceKm(Double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }
}
