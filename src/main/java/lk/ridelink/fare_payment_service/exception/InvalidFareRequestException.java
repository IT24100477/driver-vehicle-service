package lk.ridelink.fare_payment_service.exception;

public class InvalidFareRequestException extends IllegalArgumentException {

    public InvalidFareRequestException(String message) {
        super(message);
    }
}
