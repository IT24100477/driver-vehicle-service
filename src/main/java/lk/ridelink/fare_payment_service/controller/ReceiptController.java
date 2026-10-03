package lk.ridelink.fare_payment_service.controller;

import lk.ridelink.fare_payment_service.receipt.Receipt;
import lk.ridelink.fare_payment_service.receipt.ReceiptService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Receipt> getReceiptByPaymentId(@PathVariable String paymentId,
                                                       Authentication authentication) {
        Receipt receipt = receiptService.getReceiptByPaymentId(paymentId);
        assertPassengerAccess(receipt.getPassengerId(), authentication);
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/ride/{rideId}/passenger/{passengerId}")
    public ResponseEntity<Receipt> getReceiptByRideAndPassenger(@PathVariable String rideId,
                                                               @PathVariable String passengerId,
                                                               Authentication authentication) {
        assertPassengerAccess(passengerId, authentication);
        return ResponseEntity.ok(receiptService.getByRideAndPassenger(rideId, passengerId));
    }

    private void assertPassengerAccess(String passengerId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication required.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authentication.getName().equals(passengerId)) {
            throw new AccessDeniedException("Access denied. You can only access your own receipts.");
        }
    }
}
