package com.ridelink.ridemanagementservice.dto;

import com.ridelink.ridemanagementservice.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request body for updating ride status")
public class RideStatusUpdateRequest {

    @NotNull(message = "Target status is required")
    @Schema(description = "Target status: ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED", example = "ACCEPTED")
    private RideStatus status;

    @Schema(description = "Reason if status is CANCELLED", example = "Passenger requested cancellation")
    private String cancellationReason;

    @Positive(message = "Actual distance must be positive")
    @Schema(description = "Actual distance in km (required when completing ride)", example = "10.4")
    private Double actualDistanceKm;

    @PositiveOrZero(message = "Actual duration cannot be negative")
    @Schema(description = "Actual duration in minutes (required when completing ride)", example = "24")
    private Integer actualDurationMinutes;

    public RideStatusUpdateRequest() {
    }

    public RideStatusUpdateRequest(RideStatus status, String cancellationReason,
                                  Double actualDistanceKm, Integer actualDurationMinutes) {
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
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
