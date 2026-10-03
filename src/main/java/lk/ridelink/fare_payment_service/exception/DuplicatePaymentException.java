package lk.ridelink.fare_payment_service.exception;

public class DuplicatePaymentException extends IllegalStateException {

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
