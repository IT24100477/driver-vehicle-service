package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.Receipt;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptResponse(
        Long receiptId,
        Long paymentId,
        String receiptNumber,
        Long rideId,
        Long passengerId,
        BigDecimal amount,
        Instant issuedAt
) {

    public static ReceiptResponse from(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getPaymentId(),
                receipt.getReceiptNumber(),
                receipt.getRideId(),
                receipt.getPassengerId(),
                receipt.getAmount(),
                receipt.getIssuedAt());
    }
}
