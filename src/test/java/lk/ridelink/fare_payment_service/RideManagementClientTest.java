package lk.ridelink.fare_payment_service;

import lk.ridelink.fare_payment_service.client.RideManagementClient;
import lk.ridelink.fare_payment_service.client.RideSummaryResponse;
import lk.ridelink.fare_payment_service.exception.RideNotFoundException;
import lk.ridelink.fare_payment_service.exception.RideServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RideManagementClientTest {

    private RideManagementClient rideManagementClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8082");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        rideManagementClient = new RideManagementClient(builder.build());
    }

    @Test
    void shouldFetchRideSummarySuccessfully() {
        mockServer.expect(requestTo("http://localhost:8082/api/rides/ride-1"))
                .andRespond(withSuccess("{\"rideId\":\"ride-1\",\"passengerId\":\"passenger-123\",\"status\":\"COMPLETED\",\"distanceKm\":12.5,\"currency\":\"LKR\"}", MediaType.APPLICATION_JSON));

        RideSummaryResponse response = rideManagementClient.getRideSummary("ride-1");

        assertEquals("ride-1", response.getRideId());
        assertEquals("passenger-123", response.getPassengerId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(12.5, response.getDistanceKm());
    }

    @Test
    void shouldThrowNotFoundWhenRideDoesNotExist() {
        mockServer.expect(requestTo("http://localhost:8082/api/rides/ride-missing"))
                .andRespond(withBadRequest());

        assertThrows(RideNotFoundException.class, () -> rideManagementClient.getRideSummary("ride-missing"));
    }

    @Test
    void shouldThrowServiceUnavailableWhenRemoteServiceFails() {
        mockServer.expect(requestTo("http://localhost:8082/api/rides/ride-err"))
                .andRespond(withServerError());

        assertThrows(RideServiceUnavailableException.class, () -> rideManagementClient.getRideSummary("ride-err"));
    }
}
