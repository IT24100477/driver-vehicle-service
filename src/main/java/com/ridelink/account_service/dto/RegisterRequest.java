package com.ridelink.account_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role; // "ROLE_PASSENGER" or "ROLE_DRIVER"
}