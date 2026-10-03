package lk.ridelink.fare_payment_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class JwtService {

    private final JwtDecoder jwtDecoder;

    public JwtService(@Value("${jwt.jwks-uri}") String jwksUri,
                      @Value("${jwt.issuer}") String issuer,
                      @Value("${jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : validationFailure("JWT audience is invalid");
        OAuth2TokenValidator<Jwt> requiredClaimsValidator = jwt ->
                hasSubject(jwt) && hasRole(jwt)
                        ? OAuth2TokenValidatorResult.success()
                        : validationFailure("JWT must contain a subject and role claim");

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                audienceValidator,
                requiredClaimsValidator));
        this.jwtDecoder = decoder;
    }

    public Jwt parseToken(String token) {
        return jwtDecoder.decode(token);
    }

    public List<String> extractRoles(Jwt jwt) {
        Object role = jwt.getClaims().get("role");
        if (role instanceof String value) {
            return List.of(value);
        }
        if (role instanceof Collection<?> values) {
            return values.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean hasSubject(Jwt jwt) {
        return jwt.getSubject() != null && !jwt.getSubject().isBlank();
    }

    private static boolean hasRole(Jwt jwt) {
        Object role = jwt.getClaims().get("role");
        return role instanceof String value && !value.isBlank()
                || role instanceof Collection<?> values && !values.isEmpty()
                && values.stream().allMatch(value -> value instanceof String string && !string.isBlank());
    }

    private static OAuth2TokenValidatorResult validationFailure(String description) {
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, description, null));
    }
}
