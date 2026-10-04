package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication Response containing JWT token and basic user details")
public class AuthResponse {

    @Schema(description = "Signed JWT token (null during registration)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Unique user identifier", example = "6abd6c498b7367faa9aa1a81")
    private String userId;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email of the authenticated user", example = "john@example.com")
    private String email;

    @Schema(description = "User role", example = "ROLE_PASSENGER")
    private String role;

    @Schema(description = "Account status", example = "ACTIVE")
    private String status;

    @Schema(description = "Informational status message", example = "Login successful!")
    private String message;
}