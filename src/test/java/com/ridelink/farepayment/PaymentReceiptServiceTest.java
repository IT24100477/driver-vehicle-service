package com.ridelink.farepayment;

import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.exception.DuplicatePaymentException;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import com.ridelink.farepayment.service.FareService;
import com.ridelink.farepayment.service.PaymentService;
import com.ridelink.farepayment.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PaymentReceiptServiceTest {

    @Autowired
    private FareService fareService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinalFareRepository finalFareRepository;

    @BeforeEach
    void cleanDatabase() {
        receiptRepository.deleteAll();
        paymentRepository.deleteAll();
        finalFareRepository.deleteAll();
    }

    @Test
    void successfulPaymentUsesFinalFareAmountAndCreatesReceipt() {
        fareService.createFinalFare(new FinalFareRequest(301L, 401L, new BigDecimal("5.00"), 10));

        Payment payment = paymentService.processPayment(301L, 401L, PaymentMethod.CARD);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(receiptRepository.findByPayment_Id(payment.getId())).isPresent();
    }

    @Test
    void preventsDuplicateSuccessfulPaymentForSameRide() {
        fareService.createFinalFare(new FinalFareRequest(302L, 402L, new BigDecimal("5.00"), 10));
        paymentService.processPayment(302L, 402L, PaymentMethod.CARD);

        assertThatThrownBy(() -> paymentService.processPayment(302L, 402L, PaymentMethod.MOBILE))
                .isInstanceOf(DuplicatePaymentException.class);
    }

    @Test
    void retrievesReceiptByPaymentId() {
        fareService.createFinalFare(new FinalFareRequest(303L, 403L, new BigDecimal("3.00"), 8));
        Payment payment = paymentService.processPayment(303L, 403L, PaymentMethod.CASH);

        var receipt = receiptService.getReceiptByPaymentId(payment.getId());

        assertThat(receipt.getPaymentId()).isEqualTo(payment.getId());
        assertThat(receipt.getReceiptNumber()).startsWith("RCT-");
        assertThat(receipt.getAmount()).isEqualByComparingTo(payment.getAmount());
    }
}
