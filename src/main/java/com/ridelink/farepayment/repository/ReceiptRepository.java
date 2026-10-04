package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReceiptRepository extends MongoRepository<Receipt, Long> {

    Optional<Receipt> findByPaymentId(Long paymentId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);
}
