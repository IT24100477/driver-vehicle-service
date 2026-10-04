package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.model.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public class DriverAvailabilityUpdateRequest {

    @NotNull(message = "Availability status is required (AVAILABLE, BUSY, OFFLINE)")
    private AvailabilityStatus availabilityStatus;

    public DriverAvailabilityUpdateRequest() {
    }

    public DriverAvailabilityUpdateRequest(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
