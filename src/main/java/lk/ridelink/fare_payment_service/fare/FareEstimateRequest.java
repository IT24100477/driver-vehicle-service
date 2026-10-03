package lk.ridelink.fare_payment_service.fare;

import java.math.BigDecimal;
import java.util.Objects;

public class FareEstimateRequest {

    private BigDecimal distanceKm;
    private BigDecimal additionalCharges;

    public FareEstimateRequest() {
        this.additionalCharges = BigDecimal.ZERO;
    }

    public FareEstimateRequest(BigDecimal distanceKm, BigDecimal additionalCharges) {
        this.distanceKm = distanceKm;
        this.additionalCharges = additionalCharges != null ? additionalCharges : BigDecimal.ZERO;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(BigDecimal additionalCharges) {
        this.additionalCharges = additionalCharges != null ? additionalCharges : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FareEstimateRequest that = (FareEstimateRequest) o;
        return Objects.equals(distanceKm, that.distanceKm) && Objects.equals(additionalCharges, that.additionalCharges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceKm, additionalCharges);
    }
}
