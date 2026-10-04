package com.ridelink.farepayment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_receipts_payment_id", columnNames = "payment_id"),
        @UniqueConstraint(name = "uk_receipts_receipt_number", columnNames = "receipt_number")
})
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "receipt_number", nullable = false, length = 80)
    private String receiptNumber;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    protected Receipt() {
    }

    public Receipt(Payment payment, String receiptNumber) {
        this.payment = payment;
        this.receiptNumber = receiptNumber;
        this.rideId = payment.getRideId();
        this.passengerId = payment.getPassengerId();
        this.amount = payment.getAmount();
    }

    @PrePersist
    void prePersist() {
        if (issuedAt == null) {
            issuedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public Long getPaymentId() {
        return payment.getId();
    }

    public String getReceiptNumber() {
        return receiptNumber;
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

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
