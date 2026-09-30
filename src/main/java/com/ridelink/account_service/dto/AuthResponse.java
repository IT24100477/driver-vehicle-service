package com.ridelink.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token; // The RS256 JWT token
    private String email;
    private String role;
    private String message;
}