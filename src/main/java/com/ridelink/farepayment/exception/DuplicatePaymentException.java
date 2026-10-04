package com.ridelink.farepayment.exception;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
