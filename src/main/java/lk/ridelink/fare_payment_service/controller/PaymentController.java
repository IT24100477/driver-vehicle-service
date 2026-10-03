 

package lk.ridelink.fare_payment_service.controller;

import jakarta.validation.Valid;
import lk.ridelink.fare_payment_service.exception.PaymentNotFoundException;
import lk.ridelink.fare_payment_service.payment.Payment;
import lk.ridelink.fare_payment_service.payment.PaymentRequest;
import lk.ridelink.fare_payment_service.payment.PaymentResponse;
import lk.ridelink.fare_payment_service.payment.PaymentService;
import lk.ridelink.fare_payment_service.receipt.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;

    public PaymentController(PaymentService paymentService, ReceiptService receiptService) {
        this.paymentService = paymentService;
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
                                                      Authentication authentication) {
        assertPassengerAccess(request.getPassengerId(), authentication);

        Payment payment = paymentService.createPayment(
                request.getRideId(),
                request.getPassengerId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod()
        );

        if (payment != null && payment.getStatus() != null && payment.getStatus().name().equals("SUCCESS")) {
            receiptService.createReceipt(payment);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String paymentId,
                                                        Authentication authentication) {
        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
        }

        assertPassengerAccess(payment.getPassengerId(), authentication);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    private void assertPassengerAccess(String passengerId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication required.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authentication.getName().equals(passengerId)) {
            throw new AccessDeniedException("Access denied. You can only access your own payment records.");
        }
    }
}