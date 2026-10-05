package com.ridelink.ridemanagementservice.repository;

import com.ridelink.ridemanagementservice.model.Ride;
import com.ridelink.ridemanagementservice.model.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends MongoRepository<Ride, String> {
    List<Ride> findByPassengerId(String passengerId);
    List<Ride> findByDriverId(String driverId);
    List<Ride> findByStatus(RideStatus status);
}
