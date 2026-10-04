package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Profile Details Response")
public class UserProfileResponse {

    @Schema(description = "Unique user identifier", example = "6abd6c498b7367faa9aa1a81")
    private String id;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "User email address", example = "john@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+94771234567")
    private String phoneNumber;

    @Schema(description = "Assigned user role", example = "ROLE_PASSENGER")
    private String role;

    @Schema(description = "Current account status", example = "ACTIVE")
    private String status;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Account last updated timestamp")
    private LocalDateTime updatedAt;
}
