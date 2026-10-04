package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.security.JwtPrincipal;
import com.ridelink.farepayment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request,
                                                          Authentication authentication) {
        assertPassengerAccess(request.passengerId(), authentication);
        Payment payment = paymentService.processPayment(request.rideId(), request.passengerId(), request.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable Long paymentId, Authentication authentication) {
        Payment payment = paymentService.getPaymentById(paymentId);
        assertPassengerAccess(payment.getPassengerId(), authentication);
        return PaymentResponse.from(payment);
    }

    private void assertPassengerAccess(Long passengerId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (admin) {
            return;
        }
        if (authentication.getPrincipal() instanceof JwtPrincipal principal && principal.userId().equals(passengerId)) {
            return;
        }
        throw new AccessDeniedException("Access denied.");
    }
}
