package lk.ridelink.fare_payment_service.fare;

import java.math.BigDecimal;
import java.util.Objects;

public class FareEstimateResponse {

    private BigDecimal baseFare;
    private BigDecimal ratePerKm;
    private BigDecimal distanceKm;
    private BigDecimal additionalCharges;
    private BigDecimal estimatedFare;
    private String currency;
    private String calculationSummary;

    public FareEstimateResponse() {
    }

    public FareEstimateResponse(BigDecimal baseFare, BigDecimal ratePerKm, BigDecimal distanceKm,
                               BigDecimal additionalCharges, BigDecimal estimatedFare, String currency,
                               String calculationSummary) {
        this.baseFare = baseFare;
        this.ratePerKm = ratePerKm;
        this.distanceKm = distanceKm;
        this.additionalCharges = additionalCharges;
        this.estimatedFare = estimatedFare;
        this.currency = currency;
        this.calculationSummary = calculationSummary;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getRatePerKm() {
        return ratePerKm;
    }

    public void setRatePerKm(BigDecimal ratePerKm) {
        this.ratePerKm = ratePerKm;
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
        this.additionalCharges = additionalCharges;
    }

    public BigDecimal getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(BigDecimal estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCalculationSummary() {
        return calculationSummary;
    }

    public void setCalculationSummary(String calculationSummary) {
        this.calculationSummary = calculationSummary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FareEstimateResponse that = (FareEstimateResponse) o;
        return Objects.equals(baseFare, that.baseFare)
                && Objects.equals(ratePerKm, that.ratePerKm)
                && Objects.equals(distanceKm, that.distanceKm)
                && Objects.equals(additionalCharges, that.additionalCharges)
                && Objects.equals(estimatedFare, that.estimatedFare)
                && Objects.equals(currency, that.currency)
                && Objects.equals(calculationSummary, that.calculationSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseFare, ratePerKm, distanceKm, additionalCharges, estimatedFare, currency, calculationSummary);
    }
}
