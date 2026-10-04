# RideLink Fare & Payment Service

Java 21, Spring Boot 3.5.16, Maven, Spring Data MongoDB, JWT security and Swagger/OpenAPI.
The service runs on port 8084 and owns fare estimation, final fares, simulated payments and receipts.

## Local Setup

Start a MongoDB server on localhost:27017, or set MONGODB_URI to your MongoDB connection string.
The default database is ridelinkdb. No SQL schema or replica set is required.

In PowerShell, from the project directory:

```powershell
$env:MONGODB_URI = 'mongodb://localhost:27017/ridelinkdb'
$env:JWT_SECRET = 'replace-with-the-shared-secret-at-least-32-bytes-long'
$env:INTERNAL_API_KEY = 'replace-with-the-shared-internal-api-key'
.\mvnw.cmd spring-boot:run
```

Use the same JWT_SECRET and INTERNAL_API_KEY as the other RideLink services.
On Windows installations with a Java truststore error downloading Maven dependencies, use:

```powershell
$env:MAVEN_OPTS = '-Djavax.net.ssl.trustStoreType=Windows-ROOT'
```

Swagger: http://localhost:8084/swagger-ui.html

## API Contracts

| Method | Endpoint | Authorization |
| --- | --- | --- |
| POST | /internal/fares/estimate | X-Internal-Api-Key |
| POST | /internal/fares/final | X-Internal-Api-Key |
| POST | /api/fares/estimate | PASSENGER, DRIVER, ADMIN |
| GET | /api/fares/rides/{rideId}/final | PASSENGER, DRIVER, ADMIN |
| POST | /api/payments | PASSENGER, ADMIN |
| GET | /api/payments/{paymentId} | PASSENGER, ADMIN |
| GET | /api/receipts/payment/{paymentId} | PASSENGER, ADMIN |

Public endpoints require a Bearer JWT with trusted userId and role claims. Passengers can access
only their own payments and receipts. Internal endpoints require no JWT.
The existing request/response fields and numeric IDs are preserved.

## Persistence

Documents are stored in final_fares, payments and receipts, with scalar references between them.
Amounts and distances use MongoDB Decimal128. The fare formula is 100 + 80 * distanceKm.
Numeric IDs are allocated atomically in the service-owned fare_payment_sequences collection.

Indexes are created automatically on startup. Unique indexes protect final_fares.rideId,
payments.transactionReference, receipts.paymentId and receipts.receiptNumber. A unique sparse
index on payments.successRideId enforces one successful payment per ride: this key equals rideId
for SUCCESS and is omitted for other statuses. A MongoDB callback keeps the key synchronized
on repository writes. Failed/pending attempts do not block a successful payment, and the database
index enforces uniqueness even across concurrent service instances.

Payment and receipt writes are separate on standalone MongoDB. Receipt creation is idempotent;
retrieving a receipt or retrying a duplicate payment repairs a missed receipt write without creating
another successful payment. Duplicate fare/payment requests return HTTP 409.

If reusing existing collections, resolve duplicate data before startup can create the unique indexes.
The service does not migrate existing MySQL records automatically.

## Tests

```powershell
.\mvnw.cmd clean test
```

Tests use a test-only in-memory MongoDB-compatible wire-protocol server on an ephemeral port.
They exercise Spring Data repositories, indexes, decimal conversion, concurrent writes, business
services, controllers, JWT/RBAC, internal API keys and Swagger without Docker or a local MongoDB.
This test server is not a production MongoDB distribution; production deployment should also be
verified against your MongoDB server.
