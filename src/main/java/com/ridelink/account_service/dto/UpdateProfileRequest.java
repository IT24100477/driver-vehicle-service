package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request Payload for Updating User Profile")
public class UpdateProfileRequest {

    @Schema(description = "Updated full name of the user", example = "Johnathan Doe")
    private String name;

    @Schema(description = "Updated phone number", example = "+94779876543")
    private String phoneNumber;
}
