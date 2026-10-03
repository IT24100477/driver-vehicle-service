package lk.ridelink.fare_payment_service.exception;

public class RideServiceUnavailableException extends RuntimeException {
    public RideServiceUnavailableException(String message) {
        super(message);
    }

    public RideServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
