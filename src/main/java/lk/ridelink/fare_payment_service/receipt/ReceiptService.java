package lk.ridelink.fare_payment_service.receipt;

import lk.ridelink.fare_payment_service.exception.ReceiptNotFoundException;
import lk.ridelink.fare_payment_service.exception.InvalidPaymentStateException;
import lk.ridelink.fare_payment_service.payment.Payment;
import lk.ridelink.fare_payment_service.payment.PaymentStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public Receipt createReceipt(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Receipt can only be generated for a successful payment.");
        }

        Optional<Receipt> existing = receiptRepository.findByPaymentId(payment.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        Receipt receipt = new Receipt();
        receipt.setPaymentId(payment.getId());
        receipt.setRideId(payment.getRideId());
        receipt.setPassengerId(payment.getPassengerId());
        receipt.setTransactionReference(payment.getTransactionReference());
        receipt.setAmount(payment.getAmount());
        receipt.setCurrency(payment.getCurrency());
        receipt.setPaymentMethod(payment.getPaymentMethod());
        receipt.setPaymentStatus(payment.getStatus());
        receipt.setIssuedAt(Instant.now());

        return receiptRepository.save(receipt);
    }

    public Receipt getByPaymentId(String paymentId) {
        return receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for paymentId: " + paymentId));
    }

    public Receipt getReceiptByPaymentId(String paymentId) {
        return getByPaymentId(paymentId);
    }

    public Receipt getByRideAndPassenger(String rideId, String passengerId) {
        return receiptRepository.findByRideIdAndPassengerId(rideId, passengerId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for rideId: " + rideId + " passengerId: " + passengerId));
    }

    public Receipt updateReceiptStatus(String paymentId, PaymentStatus status) {
        Receipt receipt = getByPaymentId(paymentId);
        receipt.setPaymentStatus(status);
        return receiptRepository.save(receipt);
    }
}
