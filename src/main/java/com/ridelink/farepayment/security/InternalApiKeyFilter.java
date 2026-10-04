package com.ridelink.farepayment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    static final String HEADER_NAME = "X-Internal-Api-Key";

    private final String internalApiKey;

    public InternalApiKeyFilter(@Value("${internal.api.key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(HEADER_NAME);
        if (providedKey == null || !providedKey.equals(internalApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal API key");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))));
        filterChain.doFilter(request, response);
    }
}
