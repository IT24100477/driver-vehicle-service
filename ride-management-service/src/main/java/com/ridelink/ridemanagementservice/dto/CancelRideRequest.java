package com.ridelink.ridemanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for cancelling a ride")
public class CancelRideRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Schema(description = "Reason for ride cancellation", example = "Passenger plans changed")
    private String cancellationReason;

    public CancelRideRequest() {
    }

    public CancelRideRequest(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
