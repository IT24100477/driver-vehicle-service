package com.ridelink.ridemanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for manually assigning or auto-selecting a driver")
public class AssignDriverRequest {

    @Schema(description = "Specific Driver ID to assign (if null, system auto-queries closest eligible driver)", example = "drv-101")
    private String driverId;

    public AssignDriverRequest() {
    }

    public AssignDriverRequest(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
}
