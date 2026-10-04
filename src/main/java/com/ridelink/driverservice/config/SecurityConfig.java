package com.ridelink.driverservice.config;

import com.ridelink.driverservice.security.JwtAuthenticationFilter;
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
                        // Public / interservice query endpoints
                        .requestMatchers(HttpMethod.GET, "/api/drivers/eligible", "/api/drivers/available").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/drivers/**", "/api/vehicles/**").authenticated()
                        // Driver and Admin mutations
                        .requestMatchers(HttpMethod.POST, "/api/drivers", "/api/vehicles").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/drivers/**", "/api/vehicles/**").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/drivers/**").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/drivers/*/location").hasAnyRole("DRIVER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
