package com.ridelink.farepayment.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "final_fares")
public class FinalFare implements NumericDocument {

    @Id
    private Long id;

    @Indexed(unique = true, name = "uk_final_fares_ride_id")
    private Long rideId;
    private Long passengerId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal distanceKm;
    private Integer durationMinutes;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    private Instant createdAt;

    protected FinalFare() {
    }

    public FinalFare(Long rideId, Long passengerId, BigDecimal distanceKm, Integer durationMinutes, BigDecimal amount) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRideId() { return rideId; }
    public Long getPassengerId() { return passengerId; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
