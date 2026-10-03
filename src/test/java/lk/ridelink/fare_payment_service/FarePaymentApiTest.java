package lk.ridelink.fare_payment_service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import lk.ridelink.fare_payment_service.fare.FareEstimateRequest;
import lk.ridelink.fare_payment_service.fare.FareEstimateResponse;
import lk.ridelink.fare_payment_service.payment.Payment;
import lk.ridelink.fare_payment_service.payment.PaymentMethod;
import lk.ridelink.fare_payment_service.payment.PaymentStatus;
import lk.ridelink.fare_payment_service.payment.PaymentService;
import lk.ridelink.fare_payment_service.receipt.Receipt;
import lk.ridelink.fare_payment_service.receipt.ReceiptService;
import lk.ridelink.fare_payment_service.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lk.ridelink.fare_payment_service.security.JwtAuthenticationFilter;
import lk.ridelink.fare_payment_service.security.JwtService;
import lk.ridelink.fare_payment_service.security.SecurityConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {lk.ridelink.fare_payment_service.controller.FareController.class,
        lk.ridelink.fare_payment_service.controller.PaymentController.class,
        lk.ridelink.fare_payment_service.controller.ReceiptController.class})
@Import({JwtService.class, JwtAuthenticationFilter.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
class FarePaymentApiTest {

    private static final String ISSUER = "ridelink-auth-service";
    private static final String AUDIENCE = "ridelink-services";
    private static final String KEY_ID = "account-service-api-test-key";
    private static HttpServer jwksServer;
    private static KeyPair signingKeyPair;

    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            signingKeyPair = keyPairGenerator.generateKeyPair();
            RSAKey publicJwk = new RSAKey.Builder((RSAPublicKey) signingKeyPair.getPublic())
                    .keyID(KEY_ID)
                    .build();
            byte[] jwks = new JWKSet(publicJwk).toString().getBytes(StandardCharsets.UTF_8);
            jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            jwksServer.createContext("/.well-known/jwks.json", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                exchange.getResponseBody().write(jwks);
                exchange.close();
            });
            jwksServer.start();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.jwks-uri", () -> "http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/.well-known/jwks.json");
        registry.add("jwt.issuer", () -> ISSUER);
        registry.add("jwt.audience", () -> AUDIENCE);
    }

    @AfterAll
    static void stopJwksServer() {
        jwksServer.stop(0);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FareService fareService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ReceiptService receiptService;

    @Test
    void shouldEstimateFareWithJsonResponse() throws Exception {
        FareEstimateResponse response = new FareEstimateResponse(
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("600.00"),
                "LKR",
                "Fare = base fare + (distance × rate per km) + additional charges"
        );

        given(fareService.calculateEstimate(any(FareEstimateRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/fares/estimate")
                            .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":10.00,\"additionalCharges\":0.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(600.00));
    }

    @Test
    void shouldRejectInvalidFareRequest() throws Exception {
        given(fareService.calculateEstimate(any(FareEstimateRequest.class)))
                .willThrow(new IllegalArgumentException("distance must be greater than zero."));

            mockMvc.perform(post("/api/fares/estimate")
                            .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":0,\"additionalCharges\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreatePayment() throws Exception {
        Payment payment = new Payment();
        payment.setId("pay-1");
        payment.setRideId("ride-1");
        payment.setPassengerId("passenger@example.com");
        payment.setAmount(new BigDecimal("250.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TXN-ABC12345");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(payment.getCreatedAt());

        given(paymentService.createPayment(any(), any(), any(), any(), any())).willReturn(payment);

            mockMvc.perform(post("/api/payments")
                            .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rideId\":\"ride-1\",\"passengerId\":\"passenger@example.com\",\"amount\":250.00,\"currency\":\"LKR\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value("ride-1"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {
        Payment payment = new Payment();
        payment.setId("pay-2");
        payment.setRideId("ride-2");
        payment.setPassengerId("passenger2@example.com");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.MOBILE);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TXN-ZZ12345");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(payment.getCreatedAt());

        given(paymentService.getPaymentById("pay-2")).willReturn(payment);

                    mockMvc.perform(get("/api/payments/pay-2")
                            .header("Authorization", "Bearer " + bearerToken("passenger2@example.com", "ROLE_PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("pay-2"));
    }

    @Test
    void shouldCalculateFinalFareWithDistinctEndpoint() throws Exception {
        FareEstimateResponse response = new FareEstimateResponse(
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("600.00"),
                "LKR",
                "Final fare = base fare + (distance × rate per km) + additional charges"
        );

        given(fareService.calculateFinalFare(any(FareEstimateRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/fares/final")
                            .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":10.00,\"additionalCharges\":0.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(600.00));
    }

    @Test
    void shouldReturnReceiptForSuccessfulPayment() throws Exception {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setPaymentId("pay-1");
        receipt.setRideId("ride-1");
        receipt.setPassengerId("passenger@example.com");
        receipt.setTransactionReference("TXN-ABC12345");
        receipt.setAmount(new BigDecimal("250.00"));
        receipt.setCurrency("LKR");
        receipt.setPaymentMethod(PaymentMethod.CARD);
        receipt.setPaymentStatus(PaymentStatus.SUCCESS);
        receipt.setIssuedAt(Instant.now());

        given(receiptService.getReceiptByPaymentId("pay-1")).willReturn(receipt);

            mockMvc.perform(get("/api/receipts/payment/pay-1")
                            .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("pay-1"));
    }

    @Test
    void shouldRejectPaymentWithoutToken() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDriverPaymentRole() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + bearerToken("driver@example.com", "ROLE_DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rideId\":\"ride-1\",\"passengerId\":\"driver@example.com\",\"amount\":250.00,\"currency\":\"LKR\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectPaymentForAnotherPassenger() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + bearerToken("passenger@example.com", "ROLE_PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rideId\":\"ride-1\",\"passengerId\":\"other@example.com\",\"amount\":250.00,\"currency\":\"LKR\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isForbidden());
    }

    private String bearerToken(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .issuer(ISSUER)
                .subject(subject)
                .audience().add(AUDIENCE).and()
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(signingKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
