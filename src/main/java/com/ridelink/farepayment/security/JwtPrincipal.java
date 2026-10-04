package com.ridelink.farepayment.security;

public record JwtPrincipal(Long userId, String role) {
}
