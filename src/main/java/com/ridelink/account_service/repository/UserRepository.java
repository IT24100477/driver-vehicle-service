package com.ridelink.account_service.repository;

import com.ridelink.account_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    // This allows you to search for a user by their email during login/registration
    Optional<User> findByEmail(String email);
}