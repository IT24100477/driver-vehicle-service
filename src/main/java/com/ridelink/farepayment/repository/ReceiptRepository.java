package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByPayment_Id(Long paymentId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);
}
