package lk.ridelink.fare_payment_service.service;

import lk.ridelink.fare_payment_service.config.FareProperties;
import lk.ridelink.fare_payment_service.exception.InvalidFareRequestException;
import lk.ridelink.fare_payment_service.fare.FareEstimateRequest;
import lk.ridelink.fare_payment_service.fare.FareEstimateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareService {

    private static final int MONEY_SCALE = 2;

    private final FareProperties fareProperties;

    public FareService(FareProperties fareProperties) {
        this.fareProperties = fareProperties;
    }

    public FareProperties getFareProperties() {
        return fareProperties;
    }

    public double calculateFare(double distance) {
        FareEstimateRequest request = new FareEstimateRequest(BigDecimal.valueOf(distance), BigDecimal.ZERO);
        return calculateEstimate(request).getEstimatedFare().doubleValue();
    }

    public FareEstimateResponse calculateEstimate(FareEstimateRequest request) {
        if (request == null) {
            throw new InvalidFareRequestException("Fare estimate request is required.");
        }

        return buildEstimateResponse(request, "Fare = base fare + (distance × rate per km) + additional charges");
    }

    public FareEstimateResponse calculateFinalFare(FareEstimateRequest request) {
        if (request == null) {
            throw new InvalidFareRequestException("Final fare request is required.");
        }

        return buildEstimateResponse(request, "Final fare = base fare + (distance × rate per km) + additional charges");
    }

    private FareEstimateResponse buildEstimateResponse(FareEstimateRequest request, String summary) {
        if (request == null) {
            throw new InvalidFareRequestException("Fare request is required.");
        }

        BigDecimal distanceKm = request.getDistanceKm();
        BigDecimal additionalCharges = request.getAdditionalCharges() == null ? BigDecimal.ZERO : request.getAdditionalCharges();

        if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFareRequestException("distance must be greater than zero.");
        }

        if (additionalCharges.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFareRequestException("additional charges cannot be negative.");
        }

        BigDecimal baseFare = fareProperties.getBaseFare();
        BigDecimal ratePerKm = fareProperties.getRatePerKm();
        BigDecimal distanceCharge = distanceKm.multiply(ratePerKm);
        BigDecimal total = baseFare.add(distanceCharge).add(additionalCharges);
        BigDecimal roundedTotal = total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new FareEstimateResponse(
                baseFare.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                ratePerKm.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                distanceKm.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                additionalCharges.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                roundedTotal,
                fareProperties.getCurrency(),
            summary
        );
    }
}

