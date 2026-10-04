package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, Long> {

    boolean existsByRideIdAndStatus(Long rideId, PaymentStatus status);

    Optional<Payment> findByRideIdAndStatus(Long rideId, PaymentStatus status);

    Optional<Payment> findByTransactionReference(String transactionReference);
}
