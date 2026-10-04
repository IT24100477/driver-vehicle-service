package com.ridelink.farepayment.service;

import com.ridelink.farepayment.config.FareProperties;
import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.entity.FinalFare;
import com.ridelink.farepayment.exception.DuplicateFinalFareException;
import com.ridelink.farepayment.exception.FinalFareNotFoundException;
import com.ridelink.farepayment.repository.FinalFareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareService {

    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371.0088");
    private static final int MONEY_SCALE = 2;

    private final FareProperties fareProperties;
    private final FinalFareRepository finalFareRepository;

    public FareService(FareProperties fareProperties, FinalFareRepository finalFareRepository) {
        this.fareProperties = fareProperties;
        this.finalFareRepository = finalFareRepository;
    }

    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        BigDecimal distanceKm = calculateDistanceKm(request);
        return new FareEstimateResponse(distanceKm, calculateAmount(distanceKm));
    }

    @Transactional
    public FinalFare createFinalFare(FinalFareRequest request) {
        if (finalFareRepository.existsByRideId(request.rideId())) {
            throw new DuplicateFinalFareException("Final fare already exists for rideId: " + request.rideId());
        }

        BigDecimal distanceKm = normalizeDistance(request.distanceKm());
        BigDecimal amount = calculateAmount(distanceKm);
        return finalFareRepository.save(new FinalFare(
                request.rideId(),
                request.passengerId(),
                distanceKm,
                request.durationMinutes(),
                amount));
    }

    @Transactional(readOnly = true)
    public FinalFare getFinalFareByRideId(Long rideId) {
        return finalFareRepository.findByRideId(rideId)
                .orElseThrow(() -> new FinalFareNotFoundException("Final fare not found for rideId: " + rideId));
    }

    BigDecimal calculateAmount(BigDecimal distanceKm) {
        return fareProperties.getBaseFare()
                .add(fareProperties.getPerKmRate().multiply(distanceKm))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistanceKm(FareEstimateRequest request) {
        double pickupLat = Math.toRadians(request.pickupLatitude().doubleValue());
        double pickupLon = Math.toRadians(request.pickupLongitude().doubleValue());
        double destinationLat = Math.toRadians(request.destinationLatitude().doubleValue());
        double destinationLon = Math.toRadians(request.destinationLongitude().doubleValue());

        double deltaLat = destinationLat - pickupLat;
        double deltaLon = destinationLon - pickupLon;
        double haversine = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(pickupLat) * Math.cos(destinationLat) * Math.pow(Math.sin(deltaLon / 2), 2);
        double angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        BigDecimal distance = EARTH_RADIUS_KM.multiply(BigDecimal.valueOf(angularDistance));
        return normalizeDistance(distance);
    }

    private BigDecimal normalizeDistance(BigDecimal distanceKm) {
        return distanceKm.setScale(2, RoundingMode.HALF_UP);
    }
}
