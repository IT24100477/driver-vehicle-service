package com.ridelink.farepayment.config;

import com.ridelink.farepayment.entity.NumericDocument;
import com.ridelink.farepayment.entity.Payment;
import org.bson.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoDocumentIdCallback implements BeforeConvertCallback<NumericDocument> {

    private final ObjectProvider<MongoOperations> mongoOperations;

    public MongoDocumentIdCallback(ObjectProvider<MongoOperations> mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public NumericDocument onBeforeConvert(NumericDocument entity, String collection) {
        if (entity instanceof Payment payment) {
            payment.syncSuccessfulRideKey();
        }
        if (entity.getId() == null) {
            // Atomic counters preserve the numeric IDs in the existing REST contracts.
            Document sequence = mongoOperations.getObject().findAndModify(
                    Query.query(Criteria.where("_id").is(collection)),
                    new Update().inc("value", 1L),
                    FindAndModifyOptions.options().upsert(true).returnNew(true),
                    Document.class, "fare_payment_sequences");
            if (sequence == null) {
                throw new IllegalStateException("Could not allocate an ID for " + collection);
            }
            entity.setId(((Number) sequence.get("value")).longValue());
        }
        return entity;
    }
}
