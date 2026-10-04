package com.ridelink.farepayment.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            FinalFareNotFoundException.class,
            PaymentNotFoundException.class,
            ReceiptNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            DuplicateFinalFareException.class,
            DuplicatePaymentException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "FORBIDDEN", exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String error, String message,
                                                       HttpServletRequest request, Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                fieldErrors));
    }
}
