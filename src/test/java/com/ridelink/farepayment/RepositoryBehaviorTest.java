package com.ridelink.farepayment;

import com.ridelink.farepayment.entity.FinalFare;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.entity.Receipt;
import com.ridelink.farepayment.repository.FinalFareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfiguration.class)
class RepositoryBehaviorTest {

    @Autowired
    private FinalFareRepository finalFareRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        receiptRepository.deleteAll();
        paymentRepository.deleteAll();
        finalFareRepository.deleteAll();
    }

    @Test
    void finalFareRideIdIsUnique() {
        finalFareRepository.insert(new FinalFare(501L, 601L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));

        assertThatThrownBy(() -> finalFareRepository.insert(
                new FinalFare(501L, 602L, new BigDecimal("3.00"), 6, new BigDecimal("340.00"))))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void onlyOneSuccessfulPaymentPerRideIsAllowed() {
        FinalFare finalFare = finalFareRepository.insert(
                new FinalFare(502L, 602L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        paymentRepository.insert(new Payment(finalFare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-A"));

        assertThatThrownBy(() -> paymentRepository.insert(
                new Payment(finalFare, PaymentMethod.MOBILE, PaymentStatus.SUCCESS, "TXN-B")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void receiptPaymentIdAndReceiptNumberAreUnique() {
        FinalFare finalFare = finalFareRepository.insert(
                new FinalFare(503L, 603L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        Payment payment = paymentRepository.insert(
                new Payment(finalFare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-C"));
        receiptRepository.insert(new Receipt(payment, "RCT-1"));

        assertThat(receiptRepository.findByPaymentId(payment.getId())).isPresent();
        assertThatThrownBy(() -> receiptRepository.insert(new Receipt(payment, "RCT-2")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void unsuccessfulPaymentsDoNotBlockASuccessfulPaymentForTheRide() {
        FinalFare fare = finalFareRepository.insert(
                new FinalFare(504L, 604L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.FAILED, "TXN-FAILED-1"));
        paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.FAILED, "TXN-FAILED-2"));
        paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.PENDING, "TXN-PENDING"));
        Payment success = paymentRepository.insert(new Payment(fare, PaymentMethod.CASH, PaymentStatus.SUCCESS, "TXN-SUCCESS"));

        assertThat(paymentRepository.count()).isEqualTo(4);
        var failedDocuments = mongoTemplate.getCollection("payments")
                .find(new Document("status", "FAILED")).into(new java.util.ArrayList<>());
        assertThat(failedDocuments).allSatisfy(document -> assertThat(document).doesNotContainKey("successRideId"));
        assertThat(paymentRepository.findByRideIdAndStatus(504L, PaymentStatus.SUCCESS))
                .get().extracting(Payment::getId).isEqualTo(success.getId());
    }

    @Test
    void transactionReferencesAreUniqueAcrossPaymentStatuses() {
        FinalFare fare = finalFareRepository.insert(
                new FinalFare(505L, 605L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.FAILED, "TXN-SHARED"));

        assertThatThrownBy(() -> paymentRepository.insert(
                new Payment(fare, PaymentMethod.CASH, PaymentStatus.SUCCESS, "TXN-SHARED")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void receiptNumbersAreUniqueAcrossDifferentPayments() {
        FinalFare firstFare = finalFareRepository.insert(
                new FinalFare(506L, 606L, new BigDecimal("2.00"), 5, new BigDecimal("260.00")));
        FinalFare secondFare = finalFareRepository.insert(
                new FinalFare(507L, 607L, new BigDecimal("3.00"), 7, new BigDecimal("340.00")));
        Payment firstPayment = paymentRepository.insert(new Payment(firstFare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-1"));
        Payment secondPayment = paymentRepository.insert(new Payment(secondFare, PaymentMethod.CASH, PaymentStatus.SUCCESS, "TXN-2"));
        receiptRepository.insert(new Receipt(firstPayment, "RCT-SHARED"));

        assertThatThrownBy(() -> receiptRepository.insert(new Receipt(secondPayment, "RCT-SHARED")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void numericIdsAndDecimalAmountsRoundTripWithoutEmbeddingRelatedDocuments() {
        FinalFare fare = finalFareRepository.insert(
                new FinalFare(508L, 608L, new BigDecimal("2.53"), 5, new BigDecimal("302.40")));
        Payment payment = paymentRepository.insert(new Payment(fare, PaymentMethod.CARD, PaymentStatus.SUCCESS, "TXN-ROUNDTRIP"));
        Receipt receipt = receiptRepository.insert(new Receipt(payment, "RCT-ROUNDTRIP"));

        Document paymentDocument = mongoTemplate.getCollection("payments").find(new Document("_id", payment.getId())).first();
        assertThat(paymentDocument).isNotNull();
        assertThat(paymentDocument.get("amount")).isInstanceOf(Decimal128.class);
        assertThat(paymentDocument.get("finalFareId")).isEqualTo(fare.getId());
        assertThat(paymentDocument).doesNotContainKey("finalFare");
        assertThat(paymentDocument.get("successRideId")).isEqualTo(fare.getRideId());
        assertThat(receiptRepository.findById(receipt.getId())).get().satisfies(saved -> {
            assertThat(saved.getPaymentId()).isEqualTo(payment.getId());
            assertThat(saved.getAmount()).isEqualByComparingTo("302.40");
            assertThat(saved.getIssuedAt()).isNotNull();
        });
    }

    @Test
    void sparseUniqueIndexIsCreatedForSuccessfulPayments() {
        Document index = mongoTemplate.getCollection("payments").listIndexes()
                .into(new java.util.ArrayList<>()).stream()
                .filter(candidate -> "uk_payments_success_ride_id".equals(candidate.getString("name")))
                .findFirst().orElseThrow();

        assertThat(index.getBoolean("unique")).isTrue();
        assertThat(index.get("key", Document.class)).isEqualTo(new Document("successRideId", 1));
        assertThat(index.getBoolean("sparse")).isTrue();
    }

    @Test
    void concurrentInsertsAllocateDistinctNumericIds() throws Exception {
        java.util.concurrent.atomic.AtomicLong rideId = new java.util.concurrent.atomic.AtomicLong(600);
        var ids = ConcurrentTestSupport.concurrently(6, () -> finalFareRepository.insert(
                new FinalFare(rideId.incrementAndGet(), 700L, new BigDecimal("2.00"), 5, new BigDecimal("260.00"))).getId());

        assertThat(ids).doesNotHaveDuplicates().allSatisfy(id -> assertThat(id).isPositive());
        assertThat(finalFareRepository.count()).isEqualTo(6);
    }
}
