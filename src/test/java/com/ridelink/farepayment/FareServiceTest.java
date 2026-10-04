package com.ridelink.farepayment;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.entity.FinalFare;
import com.ridelink.farepayment.exception.DuplicateFinalFareException;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfiguration.class)
class FareServiceTest {

    @Autowired
    private FareService fareService;

    @Autowired
    private FinalFareRepository finalFareRepository;

    @BeforeEach
    void cleanDatabase() {
        finalFareRepository.deleteAll();
    }

    @Test
    void estimatesFareFromCoordinates() {
        var response = fareService.estimateFare(new FareEstimateRequest(
                new BigDecimal("6.9271"),
                new BigDecimal("79.8612"),
                new BigDecimal("6.9147"),
                new BigDecimal("79.9729")));

        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("12.41"));
        assertThat(response.estimatedFare()).isEqualByComparingTo(new BigDecimal("1092.80"));
    }

    @Test
    void persistsFinalFare() {
        FinalFare finalFare = fareService.createFinalFare(new FinalFareRequest(
                101L,
                201L,
                new BigDecimal("12.53"),
                31));

        assertThat(finalFare.getId()).isNotNull();
        assertThat(finalFare.getAmount()).isEqualByComparingTo(new BigDecimal("1102.40"));
        assertThat(finalFareRepository.findByRideId(101L)).isPresent();
    }

    @Test
    void preventsDuplicateFinalFareForSameRide() {
        FinalFareRequest request = new FinalFareRequest(102L, 202L, new BigDecimal("7.50"), 20);
        fareService.createFinalFare(request);

        assertThatThrownBy(() -> fareService.createFinalFare(request))
                .isInstanceOf(DuplicateFinalFareException.class);
    }

    @Test
    void concurrentFinalFareRequestsCreateOnlyOneFare() throws Exception {
        FinalFareRequest request = new FinalFareRequest(103L, 203L, new BigDecimal("7.50"), 20);
        var results = ConcurrentTestSupport.concurrently(6, () -> {
            try {
                fareService.createFinalFare(request);
                return true;
            } catch (DuplicateFinalFareException exception) {
                return false;
            }
        });

        assertThat(results).containsOnlyOnce(true);
        assertThat(finalFareRepository.count()).isEqualTo(1);
    }
}
