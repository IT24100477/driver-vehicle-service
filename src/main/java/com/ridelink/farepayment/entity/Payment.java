package com.ridelink.farepayment.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@CompoundIndex(name = "ix_payments_ride_status", def = "{'rideId': 1, 'status': 1}")
public class Payment implements NumericDocument {

    @Id
    private Long id;
    private Long finalFareId;
    private Long rideId;
    private Long passengerId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    @Indexed(unique = true, sparse = true, name = "uk_payments_success_ride_id")
    @Field(write = Field.Write.NON_NULL)
    private Long successRideId;

    @Indexed(unique = true, name = "uk_payments_transaction_reference")
    private String transactionReference;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(FinalFare finalFare, PaymentMethod paymentMethod, PaymentStatus status, String transactionReference) {
        this.finalFareId = finalFare.getId();
        this.rideId = finalFare.getRideId();
        this.passengerId = finalFare.getPassengerId();
        this.amount = finalFare.getAmount();
        this.paymentMethod = paymentMethod;
        this.status = status;
        syncSuccessfulRideKey();
        this.transactionReference = transactionReference;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFinalFareId() { return finalFareId; }
    public Long getRideId() { return rideId; }
    public Long getPassengerId() { return passengerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public Long getSuccessRideId() { return successRideId; }
    public String getTransactionReference() { return transactionReference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void syncSuccessfulRideKey() {
        successRideId = status == PaymentStatus.SUCCESS ? rideId : null;
    }
}
