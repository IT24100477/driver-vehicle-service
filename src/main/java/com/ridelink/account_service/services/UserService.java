package com.ridelink.account_service.services;

import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UpdateRoleRequest;
import com.ridelink.account_service.dto.UpdateStatusRequest;
import com.ridelink.account_service.dto.UserProfileResponse;
import com.ridelink.account_service.exception.UserNotFoundException;
import com.ridelink.account_service.model.User;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final List<String> ALLOWED_ROLES = List.of("ROLE_PASSENGER", "ROLE_DRIVER", "ROLE_ADMIN");
    private static final List<String> ALLOWED_STATUSES = List.of("ACTIVE", "SUSPENDED", "INACTIVE");

    @Autowired
    private UserRepository userRepository;

    public List<UserProfileResponse> getAllUsers(Optional<String> role, Optional<String> status) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(u -> role.map(r -> {
                    String formattedRole = r.trim().toUpperCase();
                    if (!formattedRole.startsWith("ROLE_")) {
                        formattedRole = "ROLE_" + formattedRole;
                    }
                    return u.getRole() != null && u.getRole().equalsIgnoreCase(formattedRole);
                }).orElse(true))
                .filter(u -> status.map(s -> u.getStatus() != null && u.getStatus().equalsIgnoreCase(s.trim())).orElse(true))
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    public UserProfileResponse getUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        User user = userRepository.findById(id.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return mapToProfileResponse(user);
    }

    public UserProfileResponse getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty!");
        }
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String id, UpdateProfileRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null!");
        }

        User user = userRepository.findById(id.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    public UserProfileResponse updateRole(String id, UpdateRoleRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        if (request == null || request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty!");
        }

        User user = userRepository.findById(id.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        String role = request.getRole().trim().toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Allowed roles: ROLE_PASSENGER, ROLE_DRIVER, ROLE_ADMIN");
        }

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    public UserProfileResponse updateStatus(String id, UpdateStatusRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        if (request == null || request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty!");
        }

        User user = userRepository.findById(id.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        String status = request.getStatus().trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: ACTIVE, SUSPENDED, INACTIVE");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    public void deleteUser(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        if (!userRepository.existsById(id.trim())) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id.trim());
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
