package lk.ridelink.fare_payment_service.exception;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(String message) {
        super(message);
    }

    public RideNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
