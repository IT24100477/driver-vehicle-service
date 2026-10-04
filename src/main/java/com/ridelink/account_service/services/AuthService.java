package com.ridelink.account_service.services;

import com.ridelink.account_service.dto.AuthResponse;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.exception.AccountSuspendedException;
import com.ridelink.account_service.exception.EmailAlreadyExistsException;
import com.ridelink.account_service.exception.InvalidCredentialsException;
import com.ridelink.account_service.model.User;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private static final List<String> ALLOWED_ROLES = List.of("ROLE_PASSENGER", "ROLE_DRIVER", "ROLE_ADMIN");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null!");
        }

        // 1. Validation for mandatory fields
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required!");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required!");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required!");
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();

        // 2. Check if email already exists
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        // 3. Format and validate role (default to ROLE_PASSENGER if null or empty)
        String role = request.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_PASSENGER";
        } else {
            role = role.trim().toUpperCase();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
        }

        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Allowed roles: ROLE_PASSENGER, ROLE_DRIVER, ROLE_ADMIN");
        }

        // 4. Create new user document
        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .role(role)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 5. Save to MongoDB
        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .token(null)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .message("User registered successfully!")
                .build();
    }

    public AuthResponse login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new InvalidCredentialsException("Email and password must not be empty!");
        }

        String normalizedEmail = email.toLowerCase().trim();

        // 1. Find user by email
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password!"));

        // 2. Verify password
        if (!passwordEncoder.matches(password.trim(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password!");
        }

        // 3. Check account status
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new AccountSuspendedException("Account is suspended. Please contact customer support.");
        }
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AccountSuspendedException("Account is inactive. Please reactivate your account.");
        }

        // 4. Generate RS256 Signed JWT Token
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .message("Login successful!")
                .build();
    }
}