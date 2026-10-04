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
@Schema(description = "Standardized Error Response Payload")
public class ErrorResponse {

    @Schema(description = "HTTP Status Code", example = "400")
    private int status;

    @Schema(description = "Error type / summary", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Email is already registered!")
    private String message;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "Endpoint path where error occurred", example = "/api/auth/register")
    private String path;
}
