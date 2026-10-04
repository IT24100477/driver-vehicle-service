package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.ReceiptResponse;
import com.ridelink.farepayment.entity.Receipt;
import com.ridelink.farepayment.security.JwtPrincipal;
import com.ridelink.farepayment.service.ReceiptService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/payment/{paymentId}")
    public ReceiptResponse getReceiptByPaymentId(@PathVariable Long paymentId, Authentication authentication) {
        Receipt receipt = receiptService.getReceiptByPaymentId(paymentId);
        assertPassengerAccess(receipt.getPassengerId(), authentication);
        return ReceiptResponse.from(receipt);
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
