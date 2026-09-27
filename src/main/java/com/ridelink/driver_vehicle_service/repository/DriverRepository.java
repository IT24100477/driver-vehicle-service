package com.ridelink.driver_vehicle_service.repository;

import com.ridelink.driver_vehicle_service.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByAvailableTrue();
}