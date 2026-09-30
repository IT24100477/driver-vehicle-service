package com.ridelink.account_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "users") // Tells MongoDB to store this in the "users" collection
@Data                           // Lombok: auto-generates getters, setters, etc.
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;          // MongoDB unique identifier
    
    private String name;
    private String email;
    private String password;    // Stores the BCrypt encrypted password
    private String role;        // "ROLE_PASSENGER" or "ROLE_DRIVER"
    private String status;      // "ACTIVE" or "INACTIVE"
}