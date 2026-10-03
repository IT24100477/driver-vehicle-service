package lk.ridelink.fare_payment_service.payment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(String rideId);

    Optional<Payment> findByRideIdAndStatus(String rideId, PaymentStatus status);

    Optional<Payment> findByTransactionReference(String transactionReference);

    Optional<Payment> findByPassengerId(String passengerId);
}