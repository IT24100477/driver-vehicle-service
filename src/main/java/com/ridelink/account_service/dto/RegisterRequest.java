package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account Registration Request Payload")
public class RegisterRequest {
    
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address for login", example = "john@example.com")
    private String email;

    @Schema(description = "Account password", example = "securepassword123")
    private String password;

    @Schema(description = "Phone number of user", example = "+94771234567")
    private String phoneNumber;

    @Schema(description = "Account role (ROLE_PASSENGER or ROLE_DRIVER)", example = "ROLE_PASSENGER", defaultValue = "ROLE_PASSENGER")
    private String role; 
}