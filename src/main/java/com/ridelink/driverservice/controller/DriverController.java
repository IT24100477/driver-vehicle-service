package com.ridelink.driverservice.controller;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Management", description = "Driver Operational Profile, Availability, Location & Eligibility")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create driver profile", description = "Initializes an operational profile for a registered driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver profile created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Role not DRIVER or ADMIN"),
            @ApiResponse(responseCode = "409", description = "Profile or license already exists")
    })
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverCreateRequest request,
                                                       Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        DriverResponse response = driverService.createDriver(request, requestingAccountId, requestingRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get driver by ID", description = "Retrieves driver operational profile by driver ID")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get driver by Account ID", description = "Retrieves driver operational profile using Account ID")
    public ResponseEntity<DriverResponse> getDriverByAccountId(@PathVariable String accountId) {
        DriverResponse response = driverService.getDriverByAccountId(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update driver details", description = "Updates driver license or service area")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable String id,
                                                       @Valid @RequestBody DriverUpdateRequest request,
                                                       Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        DriverResponse response = driverService.updateDriver(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/availability")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update driver availability status", description = "Toggles driver availability: AVAILABLE, BUSY, OFFLINE")
    public ResponseEntity<DriverResponse> updateAvailability(@PathVariable String id,
                                                             @Valid @RequestBody DriverAvailabilityUpdateRequest request,
                                                             Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        DriverResponse response = driverService.updateAvailability(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}/location", method = {RequestMethod.POST, RequestMethod.PATCH})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update driver simulated location", description = "Updates driver current simulated latitude and longitude (supports POST and PATCH)")
    public ResponseEntity<DriverResponse> updateLocation(@PathVariable String id,
                                                         @Valid @RequestBody DriverLocationUpdateRequest request,
                                                         Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        DriverResponse response = driverService.updateLocation(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available active drivers", description = "Lists all drivers currently marked ACTIVE and AVAILABLE")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        List<DriverResponse> response = driverService.getAvailableDrivers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eligible")
    @Operation(summary = "Query eligible drivers by pickup coordinates or service area", description = "Returns available active drivers within max radius sorted closest first")
    public ResponseEntity<List<EligibleDriverResponse>> getEligibleDrivers(
            @RequestParam(required = false) Double pickupLatitude,
            @RequestParam(required = false) Double pickupLongitude,
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false, defaultValue = "15.0") Double maxRadiusKm) {
        List<EligibleDriverResponse> response = driverService.getEligibleDrivers(pickupLatitude, pickupLongitude, serviceArea, maxRadiusKm);
        return ResponseEntity.ok(response);
    }

    private String getFirstRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }
}
