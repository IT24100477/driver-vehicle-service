package com.ridelink.ridemanagementservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Geographic or address location details")
public class Location {

    @NotNull(message = "Latitude is required")
    @Schema(description = "Latitude coordinate", example = "6.9271")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Schema(description = "Longitude coordinate", example = "79.8612")
    private Double longitude;

    @NotBlank(message = "Address or place name is required")
    @Schema(description = "Human-readable address or landmark", example = "Colombo Fort Railway Station")
    private String address;

    @Schema(description = "Service area name", example = "Colombo")
    private String area;

    public Location() {
    }

    public Location(Double latitude, Double longitude, String address, String area) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.area = area;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
