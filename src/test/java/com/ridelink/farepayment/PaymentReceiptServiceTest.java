package com.ridelink.farepayment;

import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.Receipt;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfiguration.class)
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
        assertThat(receiptRepository.findByPaymentId(payment.getId())).isPresent();
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

    @Test
    void concurrentPaymentRequestsCreateOnlyOneSuccessfulPaymentAndReceipt() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(304L, 404L, new BigDecimal("3.00"), 8));

        var results = ConcurrentTestSupport.concurrently(6, () -> {
            try {
                paymentService.processPayment(304L, 404L, PaymentMethod.CARD);
                return true;
            } catch (DuplicatePaymentException exception) {
                return false;
            }
        });

        assertThat(results).containsOnlyOnce(true);
        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(receiptRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentReceiptCreationReturnsTheSameReceipt() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(305L, 405L, new BigDecimal("3.00"), 8));
        Payment payment = paymentService.processPayment(305L, 405L, PaymentMethod.CARD);
        receiptRepository.deleteAll();

        var receipts = ConcurrentTestSupport.concurrently(6, () -> receiptService.createReceipt(payment));

        assertThat(receipts).extracting(Receipt::getId).containsOnly(receipts.getFirst().getId());
        assertThat(receiptRepository.count()).isEqualTo(1);
    }

    @Test
    void receiptRetrievalRepairsMissingReceiptForSuccessfulPayment() {
        fareService.createFinalFare(new FinalFareRequest(306L, 406L, new BigDecimal("3.00"), 8));
        Payment payment = paymentService.processPayment(306L, 406L, PaymentMethod.CASH);
        receiptRepository.deleteAll();

        Receipt recoveredReceipt = receiptService.getReceiptByPaymentId(payment.getId());

        assertThat(recoveredReceipt.getPaymentId()).isEqualTo(payment.getId());
        assertThat(receiptRepository.count()).isEqualTo(1);
        assertThat(receiptService.getReceiptByPaymentId(payment.getId()).getId()).isEqualTo(recoveredReceipt.getId());
    }

    @Test
    void duplicatePaymentRetryRepairsMissingReceiptWithoutChargingAgain() {
        fareService.createFinalFare(new FinalFareRequest(307L, 407L, new BigDecimal("3.00"), 8));
        paymentService.processPayment(307L, 407L, PaymentMethod.CASH);
        receiptRepository.deleteAll();

        assertThatThrownBy(() -> paymentService.processPayment(307L, 407L, PaymentMethod.CARD))
                .isInstanceOf(DuplicatePaymentException.class);
        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(receiptRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsReceiptForFailedPayment() {
        var fare = fareService.createFinalFare(new FinalFareRequest(308L, 408L, new BigDecimal("3.00"), 8));
        Payment payment = paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.FAILED, "TXN-FAILED"));

        assertThatThrownBy(() -> receiptService.createReceipt(payment)).isInstanceOf(IllegalArgumentException.class);
        assertThat(receiptRepository.count()).isZero();
    }
}
