package lk.ridelink.fare_payment_service.payment;

import lk.ridelink.fare_payment_service.config.FareProperties;
import lk.ridelink.fare_payment_service.exception.DuplicatePaymentException;
import lk.ridelink.fare_payment_service.exception.InvalidPaymentStateException;
import lk.ridelink.fare_payment_service.exception.PaymentNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentService {

    private static final int MONEY_SCALE = 2;
    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            PaymentStatus.PENDING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED),
            PaymentStatus.SUCCESS, Set.of(PaymentStatus.REFUNDED),
            PaymentStatus.FAILED, Set.of(PaymentStatus.PENDING),
            PaymentStatus.REFUNDED, Set.of()
    );

    private final PaymentRepository paymentRepository;
    private final FareProperties fareProperties;

    public PaymentService(PaymentRepository paymentRepository, FareProperties fareProperties) {
        this.paymentRepository = paymentRepository;
        this.fareProperties = fareProperties;
    }

    public String makePayment(double amount) {
        if (amount <= 0) {
            return "Payment failed: amount must be greater than 0";
        }

        Payment payment = createPayment(
                "legacy-ride-" + UUID.randomUUID(),
                "legacy-passenger",
                BigDecimal.valueOf(amount),
                fareProperties.getCurrency(),
                PaymentMethod.CARD
        );

        return "Payment successful: " + payment.getCurrency() + " " + payment.getAmount();
    }

    public Payment createPayment(String rideId, String passengerId, BigDecimal amount, String currency,
                                PaymentMethod paymentMethod) {
        validateRequiredFields(rideId, passengerId, amount, currency, paymentMethod);

        BigDecimal normalizedAmount = normalizeMoney(amount);
        Optional<Payment> successfulPayment = safeFindByRideIdAndStatus(rideId, PaymentStatus.SUCCESS);
        if (successfulPayment.isPresent()) {
            throw new DuplicatePaymentException("Duplicate successful payment detected for ride: " + rideId);
        }

        Optional<Payment> pendingPayment = safeFindByRideIdAndStatus(rideId, PaymentStatus.PENDING);
        if (pendingPayment.isPresent()) {
            throw new InvalidPaymentStateException("A payment is already pending for ride: " + rideId);
        }

        Payment payment = new Payment(
                rideId,
                passengerId,
                normalizedAmount,
                normalizeCurrency(currency),
                paymentMethod,
                PaymentStatus.SUCCESS,
                generateTransactionReference()
        );

        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(payment.getCreatedAt());
        return savePayment(payment);
    }

    public Payment getPaymentById(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId is required.");
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for id: " + paymentId));
    }

    public Payment createPendingPayment(String rideId, String passengerId, BigDecimal amount, String currency,
                                       PaymentMethod paymentMethod) {
        validateRequiredFields(rideId, passengerId, amount, currency, paymentMethod);

        Optional<Payment> successfulPayment = safeFindByRideIdAndStatus(rideId, PaymentStatus.SUCCESS);
        if (successfulPayment.isPresent()) {
            throw new DuplicatePaymentException("Duplicate successful payment detected for ride: " + rideId);
        }

        Payment payment = new Payment(
                rideId,
                passengerId,
                normalizeMoney(amount),
                normalizeCurrency(currency),
                paymentMethod,
                PaymentStatus.PENDING,
                generateTransactionReference()
        );
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(payment.getCreatedAt());
        return savePayment(payment);
    }

    public Payment savePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }

        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(Instant.now());
        }
        if (payment.getUpdatedAt() == null) {
            payment.setUpdatedAt(payment.getCreatedAt());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment == null ? payment : savedPayment;
    }

    public Payment updatePaymentStatus(Payment payment, PaymentStatus newStatus) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }

        PaymentStatus currentStatus = payment.getStatus();
        if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            throw new InvalidPaymentStateException(
                    "Invalid payment status transition from " + currentStatus + " to " + newStatus);
        }

        payment.setStatus(newStatus);
        payment.setUpdatedAt(Instant.now());
        return savePayment(payment);
    }

    private void validateRequiredFields(String rideId, String passengerId, BigDecimal amount,
                                       String currency, PaymentMethod paymentMethod) {
        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException("ride id is required.");
        }
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("passenger id is required.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("payment method is required.");
        }
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount is required.");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency == null ? fareProperties.getCurrency() : currency.trim();
        if (normalized.isBlank()) {
            return fareProperties.getCurrency();
        }
        return normalized.toUpperCase();
    }

    private Optional<Payment> safeFindByRideIdAndStatus(String rideId, PaymentStatus status) {
        Optional<Payment> result = paymentRepository.findByRideIdAndStatus(rideId, status);
        return result == null ? Optional.empty() : result;
    }

    private Optional<Payment> safeFindByTransactionReference(String transactionReference) {
        Optional<Payment> result = paymentRepository.findByTransactionReference(transactionReference);
        return result == null ? Optional.empty() : result;
    }

    private String generateTransactionReference() {
        String transactionReference;
        do {
            transactionReference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (safeFindByTransactionReference(transactionReference).isPresent());
        return transactionReference;
    }
}

