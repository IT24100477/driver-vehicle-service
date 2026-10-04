package com.ridelink.farepayment;

import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import com.ridelink.farepayment.service.FareService;
import com.ridelink.farepayment.service.PaymentService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarePaymentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FareService fareService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinalFareRepository finalFareRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {
        receiptRepository.deleteAll();
        paymentRepository.deleteAll();
        finalFareRepository.deleteAll();
    }

    @Test
    void swaggerEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, startsWith("/swagger-ui/index.html")));
    }

    @Test
    void internalApiKeyIsRequired() throws Exception {
        mockMvc.perform(post("/internal/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalApiKeyAllowsFareEstimateWithoutJwt() throws Exception {
        mockMvc.perform(post("/internal/fares/estimate")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(12.41))
                .andExpect(jsonPath("$.estimatedFare").value(1092.80));
    }

    @Test
    void internalApiKeyIgnoresInvalidJwt() throws Exception {
        mockMvc.perform(post("/internal/fares/estimate")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(1092.80));
    }

    @Test
    void internalApiKeyAllowsFinalFareCreationWithoutJwt() throws Exception {
        mockMvc.perform(post("/internal/fares/final")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": 701,
                                  "passengerId": 801,
                                  "distanceKm": 5.50,
                                  "durationMinutes": 12
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value(701))
                .andExpect(jsonPath("$.amount").value(540.00));
    }

    @Test
    void controllerValidationRejectsInvalidInternalRequest() throws Exception {
        mockMvc.perform(post("/internal/fares/estimate")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void publicFareApisRequireJwtAndAllowDriverPassengerAdminRoles() throws Exception {
        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/fares/estimate")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(901L, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(1092.80));
    }

    @Test
    void roleRestrictionsRejectDriverFromPayments() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(702L, 802L, new BigDecimal("5.00"), 10));

        mockMvc.perform(post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(802L, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": 702,
                                  "passengerId": 802,
                                  "paymentMethod": "CARD"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void paymentSuccessCreatesReceiptAndPreventsDuplicateSuccessfulPayment() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(703L, 803L, new BigDecimal("5.00"), 10));
        String token = bearerToken(803L, "PASSENGER");

        mockMvc.perform(post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": 703,
                                  "passengerId": 803,
                                  "paymentMethod": "CARD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(500.00));

        mockMvc.perform(post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": 703,
                                  "passengerId": 803,
                                  "paymentMethod": "MOBILE"
                                }
                                """))
                .andExpect(status().isConflict());

        org.assertj.core.api.Assertions.assertThat(receiptRepository.count()).isEqualTo(1);
    }

    @Test
    void receiptCanBeRetrievedByPaymentId() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(704L, 804L, new BigDecimal("3.00"), 8));
        Payment payment = paymentService.processPayment(704L, 804L, PaymentMethod.CASH);

        mockMvc.perform(get("/api/receipts/payment/{paymentId}", payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(804L, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(payment.getId()))
                .andExpect(jsonPath("$.receiptNumber").value(startsWith("RCT-")));
    }

    @Test
    void passengerCannotAccessAnotherPassengersPayment() throws Exception {
        fareService.createFinalFare(new FinalFareRequest(705L, 805L, new BigDecimal("3.00"), 8));
        Payment payment = paymentService.processPayment(705L, 805L, PaymentMethod.CARD);

        mockMvc.perform(get("/api/payments/{paymentId}", payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(999L, "PASSENGER")))
                .andExpect(status().isForbidden());
    }

    private String estimateJson() {
        return """
                {
                  "pickupLatitude": 6.9271,
                  "pickupLongitude": 79.8612,
                  "destinationLatitude": 6.9147,
                  "destinationLongitude": 79.9729
                }
                """;
    }

    private String bearerToken(Long userId, String role) {
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId)
                .claim("role", role)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return "Bearer " + token;
    }
}
