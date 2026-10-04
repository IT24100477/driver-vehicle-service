package com.ridelink.farepayment.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "receipts")
public class Receipt implements NumericDocument {

    @Id
    private Long id;

    @Indexed(unique = true, name = "uk_receipts_payment_id")
    private Long paymentId;

    @Indexed(unique = true, name = "uk_receipts_receipt_number")
    private String receiptNumber;
    private Long rideId;
    private Long passengerId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    private Instant issuedAt;

    protected Receipt() {
    }

    public Receipt(Payment payment, String receiptNumber) {
        this.paymentId = payment.getId();
        this.receiptNumber = receiptNumber;
        this.rideId = payment.getRideId();
        this.passengerId = payment.getPassengerId();
        this.amount = payment.getAmount();
        this.issuedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaymentId() { return paymentId; }
    public String getReceiptNumber() { return receiptNumber; }
    public Long getRideId() { return rideId; }
    public Long getPassengerId() { return passengerId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getIssuedAt() { return issuedAt; }
}
