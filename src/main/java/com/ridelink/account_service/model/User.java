package com.ridelink.account_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "users") 
@Data                           
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;        
    
    private String name;
    private String email;
    private String password;    
    private String phoneNumber;
    private String role; // ROLE_PASSENGER, ROLE_DRIVER, ROLE_ADMIN
    private String status; // ACTIVE, SUSPENDED, INACTIVE

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}