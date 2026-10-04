package com.ridelink.driverservice.repository;

import com.ridelink.driverservice.model.AvailabilityStatus;
import com.ridelink.driverservice.model.Driver;
import com.ridelink.driverservice.model.OperationalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {
    Optional<Driver> findByAccountId(String accountId);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByAccountId(String accountId);
    List<Driver> findByOperationalStatusAndAvailabilityStatus(OperationalStatus operationalStatus, AvailabilityStatus availabilityStatus);
}
