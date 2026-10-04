CREATE TABLE final_fares (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ride_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    distance_km DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_final_fares_ride_id UNIQUE (ride_id)
);

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    final_fare_id BIGINT NOT NULL,
    ride_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    success_ride_id BIGINT NULL,
    transaction_reference VARCHAR(80) NOT NULL,
    failure_reason VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payments_final_fare FOREIGN KEY (final_fare_id) REFERENCES final_fares (id),
    CONSTRAINT uk_payments_transaction_reference UNIQUE (transaction_reference),
    CONSTRAINT uk_payments_success_ride_id UNIQUE (success_ride_id),
    INDEX idx_payments_ride_id (ride_id),
    INDEX idx_payments_passenger_id (passenger_id),
    INDEX idx_payments_status (status)
);

CREATE TABLE receipts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    receipt_number VARCHAR(80) NOT NULL,
    ride_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_receipts_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT uk_receipts_payment_id UNIQUE (payment_id),
    CONSTRAINT uk_receipts_receipt_number UNIQUE (receipt_number),
    INDEX idx_receipts_ride_id (ride_id),
    INDEX idx_receipts_passenger_id (passenger_id)
);
