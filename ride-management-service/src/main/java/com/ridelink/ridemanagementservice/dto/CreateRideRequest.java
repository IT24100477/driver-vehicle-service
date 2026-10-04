package com.ridelink.ridemanagementservice.dto;

import com.ridelink.ridemanagementservice.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for booking a new ride")
public class CreateRideRequest {

    @NotBlank(message = "Passenger ID is required")
    @Schema(description = "Account ID of the booking passenger", example = "acc-pas-101")
    private String passengerId;

    @NotNull(message = "Pickup location is required")
    @Valid
    @Schema(description = "Trip starting location details")
    private Location pickupLocation;

    @NotNull(message = "Destination location is required")
    @Valid
    @Schema(description = "Trip destination location details")
    private Location destinationLocation;

    @Positive(message = "Estimated distance must be positive")
    @Schema(description = "Estimated distance in km (optional, calculated if omitted)", example = "7.8")
    private Double estimatedDistanceKm;

    @Positive(message = "Estimated duration must be positive")
    @Schema(description = "Estimated trip duration in minutes", example = "18")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Automatically query and assign the closest available driver", example = "true", defaultValue = "true")
    private Boolean autoAssignDriver = true;

    public CreateRideRequest() {
    }

    public CreateRideRequest(String passengerId, Location pickupLocation, Location destinationLocation,
                             Double estimatedDistanceKm, Integer estimatedDurationMinutes, Boolean autoAssignDriver) {
        this.passengerId = passengerId;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.autoAssignDriver = (autoAssignDriver != null) ? autoAssignDriver : true;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public Double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(Double estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public Boolean getAutoAssignDriver() {
        return autoAssignDriver;
    }

    public void setAutoAssignDriver(Boolean autoAssignDriver) {
        this.autoAssignDriver = autoAssignDriver;
    }
}
