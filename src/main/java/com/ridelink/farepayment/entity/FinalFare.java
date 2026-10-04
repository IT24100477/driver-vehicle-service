package com.ridelink.farepayment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "final_fares", uniqueConstraints = {
        @UniqueConstraint(name = "uk_final_fares_ride_id", columnNames = "ride_id")
})
public class FinalFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FinalFare() {
    }

    public FinalFare(Long rideId, Long passengerId, BigDecimal distanceKm, Integer durationMinutes, BigDecimal amount) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.amount = amount;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
