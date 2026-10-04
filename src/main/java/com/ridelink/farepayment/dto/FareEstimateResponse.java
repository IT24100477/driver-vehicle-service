package com.ridelink.farepayment.dto;

import java.math.BigDecimal;

public record FareEstimateResponse(BigDecimal distanceKm, BigDecimal estimatedFare) {
}
