package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByRideIdAndStatus(Long rideId, PaymentStatus status);

    Optional<Payment> findByRideIdAndStatus(Long rideId, PaymentStatus status);

    Optional<Payment> findByTransactionReference(String transactionReference);
}
