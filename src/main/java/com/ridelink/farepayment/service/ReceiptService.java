package com.ridelink.farepayment.service;

import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.entity.Receipt;
import com.ridelink.farepayment.exception.PaymentNotFoundException;
import com.ridelink.farepayment.exception.ReceiptNotFoundException;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Service
public class ReceiptService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ReceiptService(ReceiptRepository receiptRepository, PaymentRepository paymentRepository) {
        this.receiptRepository = receiptRepository;
        this.paymentRepository = paymentRepository;
    }

    public Receipt createReceipt(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Receipt can only be generated after a successful payment.");
        }

        var existingReceipt = receiptRepository.findByPaymentId(payment.getId());
        if (existingReceipt.isPresent()) {
            return existingReceipt.get();
        }
        try {
            return receiptRepository.insert(new Receipt(payment, generateReceiptNumber()));
        } catch (DuplicateKeyException exception) {
            // A concurrent caller may already have issued this payment's receipt.
            return receiptRepository.findByPaymentId(payment.getId()).orElseThrow(() -> exception);
        }
    }

    public Receipt getReceiptByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for id: " + paymentId));
        return receiptRepository.findByPaymentId(paymentId).orElseGet(() -> {
            // Standalone MongoDB has no cross-document transactions; repair a missed write on retry.
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return createReceipt(payment);
            }
            throw new ReceiptNotFoundException("Receipt not found for paymentId: " + paymentId);
        });
    }

    private String generateReceiptNumber() {
        String receiptNumber;
        do {
            byte[] bytes = new byte[5];
            secureRandom.nextBytes(bytes);
            receiptNumber = "RCT-" + LocalDate.now(ZoneOffset.UTC).format(DATE_FORMAT) + "-"
                    + HexFormat.of().formatHex(bytes).toUpperCase();
        } while (receiptRepository.findByReceiptNumber(receiptNumber).isPresent());
        return receiptNumber;
    }
}
