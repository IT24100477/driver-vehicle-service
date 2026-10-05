package com.ridelink.ridemanagementservice.client;

import com.ridelink.ridemanagementservice.client.dto.FareCalculateRequestDto;
import com.ridelink.ridemanagementservice.client.dto.FareCalculateResponseDto;
import com.ridelink.ridemanagementservice.client.dto.FareEstimateRequestDto;
import com.ridelink.ridemanagementservice.client.dto.FareEstimateResponseDto;
import com.ridelink.ridemanagementservice.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FareServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FareServiceClient.class);

    private final RestClient restClient;

    public FareServiceClient(
            @Value("${services.fare.base-url:http://localhost:8084}") String fareServiceUrl) {
        log.info("Configuring FareServiceClient with baseUrl: {}", fareServiceUrl);
        this.restClient = RestClient.builder()
                .baseUrl(fareServiceUrl)
                .build();
    }

    public FareEstimateResponseDto estimateFare(Double distanceKm, Integer durationMinutes) {
        try {
            log.info("Calling Fare & Payment Service /api/fares/estimate for {} km, {} mins", distanceKm, durationMinutes);
            FareEstimateRequestDto request = new FareEstimateRequestDto(distanceKm, durationMinutes);
            return restClient.post()
                    .uri("/api/fares/estimate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("Fare & Payment Service returned error HTTP {}", res.getStatusCode());
                        throw new ExternalServiceException("Fare & Payment Service",
                                "Received error status: " + res.getStatusCode(), res.getStatusCode().value());
                    })
                    .body(FareEstimateResponseDto.class);
        } catch (ExternalServiceException ese) {
            throw ese;
        } catch (Exception e) {
            log.error("Failed to connect to Fare & Payment Service: {}", e.getMessage());
            throw new ExternalServiceException("Fare & Payment Service", "Service is unavailable: " + e.getMessage(), e);
        }
    }

    public FareCalculateResponseDto calculateFare(String rideId, Double distanceKm, Integer durationMinutes) {
        try {
            log.info("Calling Fare & Payment Service /api/fares/calculate for ride ID: {}, {} km, {} mins",
                    rideId, distanceKm, durationMinutes);
            FareCalculateRequestDto request = new FareCalculateRequestDto(rideId, distanceKm, durationMinutes);
            return restClient.post()
                    .uri("/api/fares/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("Fare & Payment Service returned error HTTP {}", res.getStatusCode());
                        throw new ExternalServiceException("Fare & Payment Service",
                                "Received error status: " + res.getStatusCode(), res.getStatusCode().value());
                    })
                    .body(FareCalculateResponseDto.class);
        } catch (ExternalServiceException ese) {
            throw ese;
        } catch (Exception e) {
            log.error("Failed to connect to Fare & Payment Service: {}", e.getMessage());
            throw new ExternalServiceException("Fare & Payment Service", "Service is unavailable: " + e.getMessage(), e);
        }
    }
}
