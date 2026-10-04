package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.*;
import com.ridelink.account_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management Controller", description = "Endpoints for user profile viewing/updating, role management, and account status management")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get all users (with optional filtering)", description = "Retrieves a list of all users. Can be filtered by role (e.g. ROLE_PASSENGER, ROLE_DRIVER) or status (e.g. ACTIVE, SUSPENDED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserProfileResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            @Parameter(description = "Filter by role (optional)", example = "ROLE_DRIVER")
            @RequestParam(required = false) Optional<String> role,
            @Parameter(description = "Filter by status (optional)", example = "ACTIVE")
            @RequestParam(required = false) Optional<String> status) {
        return ResponseEntity.ok(userService.getAllUsers(role, status));
    }

    @Operation(summary = "Get user profile by User ID", description = "Fetches complete profile details for a specific user ID. Essential for inter-service communication (e.g. Driver & Vehicle Service or Ride Service looking up accounts).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @Parameter(description = "Unique user ID", required = true, example = "6abd6c498b7367faa9aa1a81")
            @PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Get user profile by Email", description = "Fetches user profile details by their registered email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserProfileResponse> getUserByEmail(
            @Parameter(description = "Registered email address", required = true, example = "john@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "Update user profile details", description = "Updates editable profile information such as name and phone number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "Unique user ID", required = true)
            @PathVariable String id,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @Operation(summary = "Manage user role", description = "Updates a user's role (e.g., promote to ROLE_DRIVER or ROLE_ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserProfileResponse> updateRole(
            @Parameter(description = "Unique user ID", required = true)
            @PathVariable String id,
            @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @Operation(summary = "Manage account status", description = "Updates account status (ACTIVE, SUSPENDED, INACTIVE). Suspended users are prevented from logging in.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status updated",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserProfileResponse> updateStatus(
            @Parameter(description = "Unique user ID", required = true)
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }

    @Operation(summary = "Delete / Deactivate user account", description = "Deletes a user account record by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User account deleted"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "Unique user ID", required = true)
            @PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully with ID: " + id));
    }
}
