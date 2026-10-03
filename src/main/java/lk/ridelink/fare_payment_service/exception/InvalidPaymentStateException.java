package lk.ridelink.fare_payment_service.exception;

public class InvalidPaymentStateException extends IllegalStateException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
