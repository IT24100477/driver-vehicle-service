package com.ridelink.farepayment.service;

import com.ridelink.farepayment.entity.FinalFare;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.exception.DuplicatePaymentException;
import com.ridelink.farepayment.exception.FinalFareNotFoundException;
import com.ridelink.farepayment.exception.PaymentNotFoundException;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FinalFareRepository finalFareRepository;
    private final ReceiptService receiptService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PaymentService(PaymentRepository paymentRepository,
                          FinalFareRepository finalFareRepository,
                          ReceiptService receiptService) {
        this.paymentRepository = paymentRepository;
        this.finalFareRepository = finalFareRepository;
        this.receiptService = receiptService;
    }

    @Transactional
    public Payment processPayment(Long rideId, Long passengerId, PaymentMethod paymentMethod) {
        FinalFare finalFare = finalFareRepository.findByRideId(rideId)
                .orElseThrow(() -> new FinalFareNotFoundException("Final fare not found for rideId: " + rideId));

        if (!finalFare.getPassengerId().equals(passengerId)) {
            throw new IllegalArgumentException("passengerId does not match final fare passengerId.");
        }

        if (paymentRepository.existsByRideIdAndStatus(rideId, PaymentStatus.SUCCESS)) {
            throw new DuplicatePaymentException("A successful payment already exists for rideId: " + rideId);
        }

        Payment payment = paymentRepository.save(new Payment(
                finalFare,
                paymentMethod,
                PaymentStatus.SUCCESS,
                generateTransactionReference()));
        receiptService.createReceipt(payment);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for id: " + paymentId));
    }

    private String generateTransactionReference() {
        String reference;
        do {
            byte[] bytes = new byte[8];
            secureRandom.nextBytes(bytes);
            reference = "TXN-" + HexFormat.of().formatHex(bytes).toUpperCase();
        } while (paymentRepository.findByTransactionReference(reference).isPresent());
        return reference;
    }
}
