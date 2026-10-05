package com.ridelink.ridemanagementservice.config;

import com.ridelink.ridemanagementservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        // Ride booking (passengers and admin)
                        .requestMatchers(HttpMethod.POST, "/api/rides").hasAnyRole("PASSENGER", "ADMIN")
                        // Driver lifecycle transitions (drivers and admin)
                        .requestMatchers(HttpMethod.PATCH, "/api/rides/*/accept").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rides/*/start").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rides/*/complete").hasAnyRole("DRIVER", "ADMIN")
                        // Cancellation, queries and assignments
                        .requestMatchers(HttpMethod.PATCH, "/api/rides/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/assign").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/rides/*/status").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/rides/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
