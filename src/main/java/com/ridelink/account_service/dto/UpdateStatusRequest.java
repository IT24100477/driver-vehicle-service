package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request Payload for Updating Account Status")
public class UpdateStatusRequest {

    @Schema(description = "New account status (e.g. ACTIVE, SUSPENDED, INACTIVE)", example = "SUSPENDED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
