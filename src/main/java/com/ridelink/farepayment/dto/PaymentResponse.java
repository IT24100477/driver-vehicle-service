package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long paymentId,
        Long rideId,
        Long passengerId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
