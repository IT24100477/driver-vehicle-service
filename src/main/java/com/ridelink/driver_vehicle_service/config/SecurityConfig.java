package com.ridelink.driver_vehicle_service.config;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                .requestMatchers(
                    "/api/drivers/**",
                    "/api/vehicles/**"
                ).hasAnyRole("DRIVER", "ADMIN")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {

        ClassPathResource resource =
                new ClassPathResource("keys/ridelink-public.crt");

        CertificateFactory factory =
                CertificateFactory.getInstance("X.509");

        try (InputStream inputStream = resource.getInputStream()) {

            X509Certificate certificate =
                    (X509Certificate) factory.generateCertificate(inputStream);

            RSAPublicKey publicKey =
                    (RSAPublicKey) certificate.getPublicKey();

            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        }
    }
}