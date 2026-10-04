package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.FinalFare;

import java.math.BigDecimal;

public record FinalFareResponse(Long rideId, BigDecimal distanceKm, Integer durationMinutes, BigDecimal amount) {

    public static FinalFareResponse from(FinalFare finalFare) {
        return new FinalFareResponse(
                finalFare.getRideId(),
                finalFare.getDistanceKm(),
                finalFare.getDurationMinutes(),
                finalFare.getAmount());
    }
}
