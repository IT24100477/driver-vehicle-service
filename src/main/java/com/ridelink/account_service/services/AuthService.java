package com.ridelink.account_service.services;

import com.ridelink.account_service.dto.AuthResponse;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.model.User;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }

        // 2. Create new user document
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Hash the password securely
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Default role (e.g. ROLE_PASSENGER or ROLE_DRIVER)
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_PASSENGER");
        user.setStatus("ACTIVE");

        // 3. Save to MongoDB
        userRepository.save(user);

        return new AuthResponse(null, user.getEmail(), user.getRole(), "User registered successfully!");
    }

    public AuthResponse login(String email, String password) {
        // 1. Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password!");
        }

        User user = userOpt.get();

        // 2. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password!");
        }

        // 3. Return response (JWT token generation will be added here next)
        return new AuthResponse("SAMPLE_JWT_TOKEN", user.getEmail(), user.getRole(), "Login successful!");
    }
}