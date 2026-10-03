package lk.ridelink.fare_payment_service;

import lk.ridelink.fare_payment_service.exception.InvalidFareRequestException;
import lk.ridelink.fare_payment_service.fare.FareEstimateRequest;
import lk.ridelink.fare_payment_service.fare.FareEstimateResponse;
import lk.ridelink.fare_payment_service.service.FareService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FareServiceTest {

    private final FareService fareService = new FareService(new lk.ridelink.fare_payment_service.config.FareProperties());

    @Test
    void calculateFare_shouldReturnBaseFarePlusDistanceAndCharges() {
        FareEstimateRequest request = new FareEstimateRequest(new BigDecimal("12.5"), new BigDecimal("25.00"));

        FareEstimateResponse response = fareService.calculateEstimate(request);

        assertEquals(new BigDecimal("100.00"), response.getBaseFare());
        assertEquals(new BigDecimal("50.00"), response.getRatePerKm());
        assertEquals(new BigDecimal("12.50"), response.getDistanceKm());
        assertEquals(new BigDecimal("25.00"), response.getAdditionalCharges());
        assertEquals(new BigDecimal("750.00"), response.getEstimatedFare());
    }

    @Test
    void calculateFare_shouldRejectZeroDistance() {
        FareEstimateRequest request = new FareEstimateRequest(BigDecimal.ZERO, BigDecimal.ZERO);

        InvalidFareRequestException exception = assertThrows(InvalidFareRequestException.class,
                () -> fareService.calculateEstimate(request));

        assertTrue(exception.getMessage().contains("distance"));
    }

    @Test
    void calculateFare_shouldRejectNegativeDistance() {
        FareEstimateRequest request = new FareEstimateRequest(new BigDecimal("-1.00"), BigDecimal.ZERO);

        InvalidFareRequestException exception = assertThrows(InvalidFareRequestException.class,
                () -> fareService.calculateEstimate(request));

        assertTrue(exception.getMessage().contains("distance"));
    }

    @Test
    void calculateFare_shouldRoundToTwoDecimalPlaces() {
        FareEstimateRequest request = new FareEstimateRequest(new BigDecimal("3.333"), BigDecimal.ZERO);

        FareEstimateResponse response = fareService.calculateEstimate(request);

        assertEquals(new BigDecimal("266.65"), response.getEstimatedFare());
    }

    @Test
    void calculateFare_shouldUseConfiguredBaseFareAndRate() {
        FareService configuredService = new FareService(new lk.ridelink.fare_payment_service.config.FareProperties());
        configuredService.getFareProperties().setBaseFare(new BigDecimal("80.00"));
        configuredService.getFareProperties().setRatePerKm(new BigDecimal("40.00"));

        FareEstimateResponse response = configuredService.calculateEstimate(new FareEstimateRequest(new BigDecimal("5"), BigDecimal.ZERO));

        assertEquals(new BigDecimal("80.00"), response.getBaseFare());
        assertEquals(new BigDecimal("40.00"), response.getRatePerKm());
        assertEquals(new BigDecimal("280.00"), response.getEstimatedFare());
    }
}
