package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account Login Request Payload")
public class LoginRequest {

    @Schema(description = "Registered email address", example = "john@example.com")
    private String email;

    @Schema(description = "Account password", example = "securepassword123")
    private String password;
}