package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.FinalFare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinalFareRepository extends JpaRepository<FinalFare, Long> {

    Optional<FinalFare> findByRideId(Long rideId);

    boolean existsByRideId(Long rideId);
}
