package lk.ridelink.fare_payment_service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Jwts;
import lk.ridelink.fare_payment_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.oauth2.jwt.Jwt;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityAccessIntegrationTest {

    private static final String ISSUER = "ridelink-auth-service";
    private static final String AUDIENCE = "ridelink-services";
    private static final String KEY_ID = "account-service-test-key";
    private static HttpServer jwksServer;
    private static KeyPair signingKeyPair;
    private JwtService jwtService;

    @BeforeAll
    static void startJwksServer() throws Exception {
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
    }

    @AfterAll
    static void stopJwksServer() {
        if (jwksServer != null) {
            jwksServer.stop(0);
        }
    }

    @BeforeEach
    void createJwtService() {
        jwtService = new JwtService(
                "http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/.well-known/jwks.json",
                ISSUER,
                AUDIENCE);
    }

    @Test
    void jwtTokenShouldValidateRsaIssuerAudienceSubjectAndRole() {
        String token = generateToken("passenger@example.com", "ROLE_PASSENGER", ISSUER, AUDIENCE, Instant.now().plusSeconds(3600));

        assertTrue(jwtService.isValid(token));
        Jwt jwt = jwtService.parseToken(token);
        assertEquals("passenger@example.com", jwt.getSubject());
        assertEquals(AUDIENCE, jwt.getAudience().getFirst());
        assertEquals("ROLE_PASSENGER", jwtService.extractRoles(jwt).getFirst());
    }

    @Test
    void invalidTokenShouldBeRejected() {
        assertFalse(jwtService.isValid("not-a-valid-jwt-token"));
    }

    @Test
    void expiredTokenShouldBeRejected() {
        String token = generateToken("passenger@example.com", "ROLE_PASSENGER", ISSUER, AUDIENCE, Instant.now().minusSeconds(60));

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void wrongIssuerOrAudienceShouldBeRejected() {
        assertFalse(jwtService.isValid(generateToken(
                "passenger@example.com", "ROLE_PASSENGER", "wrong-issuer", AUDIENCE, Instant.now().plusSeconds(3600))));
        assertFalse(jwtService.isValid(generateToken(
                "passenger@example.com", "ROLE_PASSENGER", ISSUER, "wrong-audience", Instant.now().plusSeconds(3600))));
    }

    private String generateToken(String subject, String role, String issuer, String audience, Instant expiry) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .issuer(issuer)
                .subject(subject)
                .audience().add(audience).and()
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
