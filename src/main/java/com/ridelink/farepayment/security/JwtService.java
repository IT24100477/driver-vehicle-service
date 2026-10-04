package com.ridelink.farepayment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtPrincipal(extractUserId(claims), extractRole(claims));
    }

    private Long extractUserId(Claims claims) {
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value && !value.isBlank()) {
            return Long.parseLong(value);
        }
        String subject = claims.getSubject();
        if (subject != null && !subject.isBlank()) {
            return Long.parseLong(subject);
        }
        throw new IllegalArgumentException("JWT must contain userId claim.");
    }

    private String extractRole(Claims claims) {
        Object role = claims.get("role");
        if (role instanceof String value && !value.isBlank()) {
            return value.startsWith("ROLE_") ? value.substring("ROLE_".length()) : value;
        }
        throw new IllegalArgumentException("JWT must contain role claim.");
    }
}
