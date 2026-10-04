package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FareEstimateRequest(
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal pickupLatitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal pickupLongitude,
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal destinationLatitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal destinationLongitude
) {
}
