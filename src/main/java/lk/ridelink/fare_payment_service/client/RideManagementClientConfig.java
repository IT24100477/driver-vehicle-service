package lk.ridelink.fare_payment_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RideManagementClientConfig {

    @Bean
    public RestClient rideManagementRestClient(@Value("${ride.management.base-url:${RIDE_MANAGEMENT_BASE_URL:http://localhost:8082}}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
