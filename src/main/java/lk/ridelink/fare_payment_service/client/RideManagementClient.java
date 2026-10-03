package lk.ridelink.fare_payment_service.client;

import lk.ridelink.fare_payment_service.exception.RideNotFoundException;
import lk.ridelink.fare_payment_service.exception.RideServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class RideManagementClient {

    private final RestClient restClient;

    public RideManagementClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public RideSummaryResponse getRideSummary(String rideId) {
        try {
            return restClient.get()
                    .uri("/api/rides/{rideId}", rideId)
                    .retrieve()
                    .body(RideSummaryResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RideNotFoundException("Ride not found: " + rideId, e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw new RideServiceUnavailableException("Ride service unavailable for ride: " + rideId, e);
            }
            throw new RideNotFoundException("Ride request failed for: " + rideId, e);
        } catch (Exception e) {
            throw new RideServiceUnavailableException("Ride service unavailable for ride: " + rideId, e);
        }
    }
}
