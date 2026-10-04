package com.ridelink.ridemanagementservice.client;

import com.ridelink.ridemanagementservice.client.dto.EligibleDriverDto;
import com.ridelink.ridemanagementservice.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class DriverServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceClient.class);

    private final RestClient restClient;

    public DriverServiceClient(
            @Value("${services.driver.base-url:http://localhost:8082}") String driverServiceUrl) {
        log.info("Configuring DriverServiceClient with baseUrl: {}", driverServiceUrl);
        this.restClient = RestClient.builder()
                .baseUrl(driverServiceUrl)
                .build();
    }

    public List<EligibleDriverDto> getEligibleDrivers(Double pickupLat, Double pickupLon, String serviceArea, Double maxRadiusKm) {
        try {
            log.info("Calling Driver & Vehicle Service /api/drivers/eligible for lat: {}, lon: {}", pickupLat, pickupLon);
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/drivers/eligible")
                            .queryParam("pickupLatitude", pickupLat)
                            .queryParam("pickupLongitude", pickupLon)
                            .queryParam("serviceArea", serviceArea)
                            .queryParam("maxRadiusKm", (maxRadiusKm != null) ? maxRadiusKm : 15.0)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("Driver & Vehicle Service returned HTTP {}", res.getStatusCode());
                        throw new ExternalServiceException("Driver & Vehicle Service",
                                "Received error status: " + res.getStatusCode(), res.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<List<EligibleDriverDto>>() {});
        } catch (ExternalServiceException ese) {
            throw ese;
        } catch (Exception e) {
            log.error("Failed to connect to Driver & Vehicle Service: {}", e.getMessage());
            throw new ExternalServiceException("Driver & Vehicle Service", "Service is unavailable: " + e.getMessage(), e);
        }
    }
}
