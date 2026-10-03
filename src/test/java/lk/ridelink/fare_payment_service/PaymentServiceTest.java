package lk.ridelink.fare_payment_service;

import lk.ridelink.fare_payment_service.config.FareProperties;
import lk.ridelink.fare_payment_service.exception.DuplicatePaymentException;
import lk.ridelink.fare_payment_service.exception.InvalidPaymentStateException;
import lk.ridelink.fare_payment_service.payment.Payment;
import lk.ridelink.fare_payment_service.payment.PaymentMethod;
import lk.ridelink.fare_payment_service.payment.PaymentRepository;
import lk.ridelink.fare_payment_service.payment.PaymentService;
import lk.ridelink.fare_payment_service.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void createPayment_shouldCreateValidSuccessfulPayment() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());

        Payment payment = paymentService.createPayment("ride-1", "passenger-1", new BigDecimal("250.00"), "LKR", PaymentMethod.CARD);

        assertEquals("ride-1", payment.getRideId());
        assertEquals("passenger-1", payment.getPassengerId());
        assertEquals(new BigDecimal("250.00"), payment.getAmount());
        assertEquals("LKR", payment.getCurrency());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getTransactionReference());
        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void createPayment_shouldRejectAmountLessThanOrEqualToZero() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment("ride-1", "passenger-1", BigDecimal.ZERO, "LKR", PaymentMethod.CASH));

        assertTrue(exception.getMessage().contains("amount"));
    }

    @Test
    void createPayment_shouldRejectBlankRideId() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment("", "passenger-1", new BigDecimal("50.00"), "LKR", PaymentMethod.CASH));

        assertTrue(exception.getMessage().contains("ride"));
    }

    @Test
    void createPayment_shouldCreatePendingStatusForNewPayment() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());

        Payment payment = paymentService.createPendingPayment("ride-2", "passenger-2", new BigDecimal("100.00"), "LKR", PaymentMethod.MOBILE);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getTransactionReference());
    }

    @Test
    void createPayment_shouldPreventDuplicateSuccessfulPaymentForSameRide() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());
        Payment existing = new Payment();
        existing.setRideId("ride-duplicate");
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.setAmount(new BigDecimal("150.00"));
        existing.setTransactionReference("TXN-123");
        when(paymentRepository.findByRideIdAndStatus("ride-duplicate", PaymentStatus.SUCCESS)).thenReturn(Optional.of(existing));

        DuplicatePaymentException exception = assertThrows(DuplicatePaymentException.class,
                () -> paymentService.createPayment("ride-duplicate", "passenger-1", new BigDecimal("150.00"), "LKR", PaymentMethod.CASH));

        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void createPayment_shouldGenerateUniqueTransactionReference() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());

        Payment payment = paymentService.createPayment("ride-3", "passenger-3", new BigDecimal("80.00"), "LKR", PaymentMethod.CASH);

        assertNotNull(payment.getTransactionReference());
        assertTrue(payment.getTransactionReference().startsWith("TXN-"));
    }

    @Test
    void updatePaymentStatus_shouldRejectInvalidTransition() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);

        InvalidPaymentStateException exception = assertThrows(InvalidPaymentStateException.class,
                () -> paymentService.updatePaymentStatus(payment, PaymentStatus.PENDING));

        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    void savePayment_shouldPersistEntity() {
        PaymentService paymentService = new PaymentService(paymentRepository, new FareProperties());
        Payment payment = new Payment();
        payment.setRideId("ride-4");
        payment.setPassengerId("passenger-4");
        payment.setAmount(new BigDecimal("90.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TXN-UNIQUE-1");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.savePayment(payment);

        assertEquals(payment, saved);
    }
}
