package com.ridelink.farepayment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_transaction_reference", columnNames = "transaction_reference"),
        @UniqueConstraint(name = "uk_payments_success_ride_id", columnNames = "success_ride_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "final_fare_id", nullable = false)
    private FinalFare finalFare;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "success_ride_id")
    private Long successRideId;

    @Column(name = "transaction_reference", nullable = false, length = 80)
    private String transactionReference;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(FinalFare finalFare, PaymentMethod paymentMethod, PaymentStatus status, String transactionReference) {
        this.finalFare = finalFare;
        this.rideId = finalFare.getRideId();
        this.passengerId = finalFare.getPassengerId();
        this.amount = finalFare.getAmount();
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionReference = transactionReference;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        syncSuccessfulRideKey();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        syncSuccessfulRideKey();
    }

    private void syncSuccessfulRideKey() {
        successRideId = status == PaymentStatus.SUCCESS ? rideId : null;
    }

    public Long getId() {
        return id;
    }

    public FinalFare getFinalFare() {
        return finalFare;
    }

    public Long getRideId() {
        return rideId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Long getSuccessRideId() {
        return successRideId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
