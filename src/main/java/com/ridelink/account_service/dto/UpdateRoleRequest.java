package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request Payload for Updating User Role")
public class UpdateRoleRequest {

    @Schema(description = "New user role (e.g. ROLE_PASSENGER, ROLE_DRIVER, ROLE_ADMIN)", example = "ROLE_DRIVER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}
