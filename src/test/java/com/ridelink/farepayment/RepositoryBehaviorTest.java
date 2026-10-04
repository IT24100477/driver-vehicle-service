package com.ridelink.farepayment;

import com.ridelink.farepayment.entity.FinalFare;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.entity.Receipt;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryBehaviorTest {

    @Autowired
    private FinalFareRepository finalFareRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Test
    void finalFareRideIdIsUnique() {
        finalFareRepository.saveAndFlush(new FinalFare(501L, 601L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));

        assertThatThrownBy(() -> finalFareRepository.saveAndFlush(
                new FinalFare(501L, 602L, new BigDecimal("3.00"), 6, new BigDecimal("340.00"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyOneSuccessfulPaymentPerRideIsAllowed() {
        FinalFare finalFare = finalFareRepository.saveAndFlush(
                new FinalFare(502L, 602L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        paymentRepository.saveAndFlush(new Payment(finalFare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-A"));

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(
                new Payment(finalFare, PaymentMethod.MOBILE, PaymentStatus.SUCCESS, "TXN-B")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void receiptPaymentIdAndReceiptNumberAreUnique() {
        FinalFare finalFare = finalFareRepository.saveAndFlush(
                new FinalFare(503L, 603L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        Payment payment = paymentRepository.saveAndFlush(
                new Payment(finalFare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-C"));
        receiptRepository.saveAndFlush(new Receipt(payment, "RCT-1"));

        assertThat(receiptRepository.findByPayment_Id(payment.getId())).isPresent();
        assertThatThrownBy(() -> receiptRepository.saveAndFlush(new Receipt(payment, "RCT-2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
