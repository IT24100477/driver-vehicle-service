package lk.ridelink.fare_payment_service.payment;

import jakarta.validation.constraints.NotNull;

public class PaymentStatusUpdateRequest {

    @NotNull(message = "status is required")
    private PaymentStatus status;

    public PaymentStatusUpdateRequest() {
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
