package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.FinalFare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FinalFareRepository extends MongoRepository<FinalFare, Long> {

    Optional<FinalFare> findByRideId(Long rideId);

    boolean existsByRideId(Long rideId);
}
