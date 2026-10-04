package com.ridelink.ridemanagementservice.controller;

import com.ridelink.ridemanagementservice.dto.*;
import com.ridelink.ridemanagementservice.service.RideService;
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
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Ride Lifecycle Orchestration, Driver Assignment, and Trip Transitions")
@SecurityRequirement(name = "BearerAuth")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @Operation(summary = "Create ride request", description = "Passenger requests a ride. Interservice call estimates fare, and optionally queries Driver Service for closest available driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ride created and assigned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "No driver available in service area"),
            @ApiResponse(responseCode = "503", description = "External service unavailable")
    })
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request,
                                                   Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.createRide(request, requestingAccountId, requestingRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID", description = "Retrieves current ride status, assigned driver, locations, and fares")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride found"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> getRideById(@PathVariable String id) {
        RideResponse response = rideService.getRideById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Get rides by Passenger ID", description = "Retrieves all ride requests created by a passenger")
    public ResponseEntity<List<RideResponse>> getRidesByPassengerId(@PathVariable String passengerId) {
        List<RideResponse> response = rideService.getRidesByPassengerId(passengerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get rides by Driver ID", description = "Retrieves all rides assigned to or completed by a driver")
    public ResponseEntity<List<RideResponse>> getRidesByDriverId(@PathVariable String driverId) {
        List<RideResponse> response = rideService.getRidesByDriverId(driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign driver to ride", description = "Assigns a specific or next closest eligible driver to a REQUESTED ride")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver assigned"),
            @ApiResponse(responseCode = "409", description = "No driver available or invalid state")
    })
    public ResponseEntity<RideResponse> assignDriver(@PathVariable String id,
                                                     @RequestBody(required = false) AssignDriverRequest request,
                                                     Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.assignDriver(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Driver accepts ride", description = "Transitions status from ASSIGNED to ACCEPTED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride accepted"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition (e.g. from COMPLETED or CANCELLED)")
    })
    public ResponseEntity<RideResponse> acceptRide(@PathVariable String id, Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.acceptRide(id, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Driver starts ride trip", description = "Transitions status from ACCEPTED to IN_PROGRESS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride started"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ResponseEntity<RideResponse> startRide(@PathVariable String id, Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.startRide(id, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Driver completes ride trip", description = "Transitions status from IN_PROGRESS to COMPLETED and calls Fare Service to calculate final fare")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride completed and final fare calculated"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ResponseEntity<RideResponse> completeRide(@PathVariable String id,
                                                     @Valid @RequestBody(required = false) RideCompleteRequest request,
                                                     Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.completeRide(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel ride", description = "Cancels a ride if it has not yet started (status IN_PROGRESS or COMPLETED cannot be cancelled)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride cancelled"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel completed or in-progress ride")
    })
    public ResponseEntity<RideResponse> cancelRide(@PathVariable String id,
                                                   @RequestBody(required = false) CancelRideRequest request,
                                                   Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.cancelRide(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ride status directly", description = "Updates ride status according to strict transition rules")
    public ResponseEntity<RideResponse> updateRideStatus(@PathVariable String id,
                                                         @Valid @RequestBody RideStatusUpdateRequest request,
                                                         Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        RideResponse response = rideService.updateRideStatus(id, request, requestingAccountId, requestingRole);
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
