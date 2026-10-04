package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinalFareRequest(
        @NotNull @Min(1) Long rideId,
        @NotNull @Min(1) Long passengerId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal distanceKm,
        @NotNull @Min(0) Integer durationMinutes
) {
}
