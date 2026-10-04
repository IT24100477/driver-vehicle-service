package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull @Min(1) Long rideId,
        @NotNull @Min(1) Long passengerId,
        @NotNull PaymentMethod paymentMethod
) {
}
