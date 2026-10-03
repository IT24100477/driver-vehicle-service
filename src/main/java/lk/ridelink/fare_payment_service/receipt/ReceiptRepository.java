package lk.ridelink.fare_payment_service.receipt;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReceiptRepository extends MongoRepository<Receipt, String> {

    Optional<Receipt> findByPaymentId(String paymentId);

    Optional<Receipt> findByRideIdAndPassengerId(String rideId, String passengerId);
}
