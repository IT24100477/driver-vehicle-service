package com.ridelink.driverservice.controller;

import com.ridelink.driverservice.dto.VehicleRequest;
import com.ridelink.driverservice.dto.VehicleResponse;
import com.ridelink.driverservice.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for Vehicle Registration, Details, and Updates")
@SecurityRequirement(name = "BearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle", description = "Associates a vehicle with a registered driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle registered"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this driver"),
            @ApiResponse(responseCode = "409", description = "Registration number already exists")
    })
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request,
                                                         Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        VehicleResponse response = vehicleService.createVehicle(request, requestingAccountId, requestingRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieves vehicle details by vehicle ID")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable String id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get vehicles by Driver ID", description = "Retrieves all vehicles associated with a driver ID")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByDriverId(@PathVariable String driverId) {
        List<VehicleResponse> response = vehicleService.getVehiclesByDriverId(driverId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details", description = "Updates make, model, color, capacity, or registration number")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable String id,
                                                         @Valid @RequestBody VehicleRequest request,
                                                         Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        VehicleResponse response = vehicleService.updateVehicle(id, request, requestingAccountId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle by ID", description = "Removes vehicle record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this driver"),
            @ApiResponse(responseCode = "404", description = "Vehicle or driver not found")
    })
    public ResponseEntity<Void> deleteVehicle(@PathVariable String id, Authentication authentication) {
        String requestingAccountId = (authentication != null) ? (String) authentication.getPrincipal() : null;
        String requestingRole = getFirstRole(authentication);
        vehicleService.deleteVehicle(id, requestingAccountId, requestingRole);
        return ResponseEntity.noContent().build();
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
