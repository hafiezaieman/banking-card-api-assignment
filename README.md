# Card Transactions API

A Spring Boot REST API for managing card transactions, built for the **Java Backend Interview Assignment** (Delivery & Ops CFS & Core Banking, Cards IT).

> **Reminder:** all data in this project is **dummy data** used for testing. There are no real transactions, no real card data, and nothing is connected to a live banking system.

## Features

| Requirement | Where it is implemented |
|---|---|
| Spring Boot application with a clear project structure | Layered packages: `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `filter`, `config` |
| APIs testable via Postman | `postman/cards-transaction.postman-collection.json` |
| Request and response logging to a file | `RequestResponseLoggingFilter` and `logback-spring.xml`, written to `logs/cards-api.log` |
| MSSQL database (`TESTDB`) | Spring Data JPA with the Microsoft JDBC driver |
| `@Transactional` on INSERT, UPDATE and GET | `CardTransactionService` (GET uses `readOnly = true`) |
| GET API with pagination (10 records per page) | `GET /api/transactions?page=0` |
| API that calls a 3rd-party API | `GET /api/transactions/{refId}/convert?to=USD` calls open.er-api.com |

## Tech stack

- Java 21 (21.0.12.1)
- Spring Boot 4.1.1 (Spring Web, Spring Data JPA, Validation)
- Maven 3.10 (with the Maven Wrapper, `mvnw`)
- Microsoft SQL Server (local instance)
- Lombok (`@Getter` and `@Setter` on the entity and DTOs, so no manual getters and setters)
- Logback (file logging)

## Prerequisites

Install the following before running the project:

1. **JDK 21** (this project was built and tested with 21.0.12.1). Check with `java -version`.
2. **Maven 3.10**. Check with `mvn -version`. A Maven Wrapper (`mvnw`) is included in the project, so a separate Maven install is optional.
3. **Microsoft SQL Server** (Express or Developer edition) and **SQL Server Management Studio (SSMS)**.
4. **Postman** to test the APIs.
5. **Internet access**, needed only for the currency conversion endpoint.
6. **IDE** used - VS Code. **Lombok is required**: enable the Lombok plugin and annotation processing in your IDE, otherwise the generated getters and setters will show as errors.
7. **Git Bash** (or any terminal) to run the commands below. This project was run using Git Bash on Windows.

## Database setup

### 1. Create the database

Open SSMS, connect to your local instance and run:

```sql
CREATE DATABASE TESTDB;
```

You do not need to create the table manually. Hibernate creates `card_transactions` automatically on first startup (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Enable SQL Server authentication

1. In SSMS, right-click the server, choose **Properties**, then **Security**, and select **SQL Server and Windows Authentication mode**.
2. Make sure the `sa` login is enabled and has a password.
3. Restart the SQL Server service.

### 3. Enable TCP/IP on port 1433

1. Open **SQL Server Configuration Manager**.
2. Go to **SQL Server Network Configuration**, then **Protocols for \<instance\>**, and set **TCP/IP** to **Enabled**.
3. Open **TCP/IP** properties, then the **IP Addresses** tab. Under **IPAll**, set **TCP Port** to `1433` and clear **TCP Dynamic Ports**.
4. Restart the SQL Server service.

To verify, run this in PowerShell: `Test-NetConnection localhost -Port 1433`. It should show `TcpTestSucceeded : True`.

## Configuration

Edit `src/main/resources/application.properties` and set your own database credentials:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=abcd1234
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=update
server.port=8081
```

| Setting | Value used in this project |
|---|---|
| Database | `TESTDB` on `localhost:1433` |
| SQL username | `sa` |
| SQL password | `abcd1234` (local development password, dummy) |
| Server port | `8081` |

If your SQL Server password is different, change `spring.datasource.password` to match it. These credentials are for a local test database only.

## How to run

### Option 1: Git Bash with the Maven Wrapper (used for this project)

```bash
git clone https://github.com/hafiezaieman/banking-card-api-assignment
cd card-api-assignment (if not working, cd .)
./mvnw clean spring-boot:run
```

Note the colon in `spring-boot:run`. The `./mvnw` wrapper downloads the right Maven version automatically on first use, so this works even if Maven is not installed.

If you have Maven installed and prefer it, `mvn clean spring-boot:run` does the same thing.

### Option 2: IDE

Open the project, then run `CardApiAssignmentApplication` (the class with the `main` method).

### Confirm it started

The console should show lines similar to:

```
HikariPool-1 - Start completed.
=================== DATABASE CONNECTION CHECK ===================
Status   : CONNECTED
Database : TESTDB
...
Tomcat started on port 8081 (http)
Started CardApiAssignmentApplication in X seconds
```

The `DatabaseConnectionChecker` class runs once at startup and prints the connection details. `Database : TESTDB` confirms the application is connected to the right database. You should also see a new `card_transactions` table in SSMS under `TESTDB > Tables`.

## Data model

Table: `card_transactions`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT, identity | Internal primary key |
| `ref_id` | VARCHAR(50) | Unique business reference |
| `merchant_name` | VARCHAR(100) | |
| `amount` | DECIMAL(15,2) | |
| `currency` | VARCHAR(3) | Stored in upper case, e.g. `MYR` |
| `status` | VARCHAR(20) | Stored in upper case, e.g. `SUCCESS` |
| `created_at` | DATETIME | Set automatically when the record is created |

## API endpoints

Base URL: `http://localhost:8081`

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/transactions` | Create a transaction | 201 |
| GET | `/api/transactions/{refId}` | Get one transaction | 200 |
| GET | `/api/transactions?page=0` | List transactions, 10 per page, newest first | 200 |
| PUT | `/api/transactions/{refId}` | Update a transaction | 200 |
| GET | `/api/transactions/{refId}/convert?to=USD` | Convert the amount using a live exchange rate (3rd-party API) | 200 |

### Create a transaction

`POST /api/transactions`

```json
{
  "refId": "TXN0001",
  "merchantName": "Tesco",
  "amount": 120.50,
  "currency": "MYR",
  "status": "SUCCESS"
}
```

### Update a transaction

`PUT /api/transactions/TXN0001`

```json
{
  "merchantName": "Tesco Extra",
  "amount": 150.00,
  "currency": "MYR",
  "status": "SUCCESS"
}
```

The `refId` cannot be changed. It is taken from the URL.

### List with pagination

`GET /api/transactions?page=0`

- `page` is zero-based. `page=0` returns records 1 to 10, `page=1` returns records 11 to 20, and so on.
- The page size is fixed at 10.
- If `page` is omitted, it defaults to `0`.

Response shape:

```json
{
  "content": [ { "refId": "TXN0012", "...": "..." } ],
  "page": 0,
  "size": 10,
  "totalElements": 12,
  "totalPages": 2
}
```

### Currency conversion (3rd-party API call)

`GET /api/transactions/TXN0001/convert?to=USD`

Flow: **Postman, then this API, then open.er-api.com**.

1. The API loads the transaction from the database.
2. It calls `https://open.er-api.com/v6/latest/{transaction currency}` to get the latest rates (no API key needed).
3. It returns the converted amount, rounded to 2 decimal places.

Example response:

```json
{
  "refId": "TXN0001",
  "merchantName": "Tesco",
  "originalAmount": 120.50,
  "originalCurrency": "MYR",
  "targetCurrency": "USD",
  "exchangeRate": 0.2370,
  "convertedAmount": 28.56,
  "rateSource": "open.er-api.com"
}
```

The values above are illustrative. The real rate depends on the day.

### Error responses

All errors use the same format:

```json
{
  "timestamp": "2026-10-22T10:40:01.123",
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found: NOPE"
}
```

| Status | When |
|---|---|
| 400 Bad Request | Validation failure (for example a missing field, a negative amount, or a currency that is not 3 letters) or an unsupported target currency |
| 404 Not Found | `refId` does not exist |
| 409 Conflict | `refId` already exists on create |
| 502 Bad Gateway | The 3rd-party exchange rate service is unavailable or returned an invalid response |
| 500 Internal Server Error | Any other unexpected error |

## Postman collection

1. Open Postman and choose **Import**.
2. Select `postman/cards-transaction.postman-collection.json`.
3. The collection uses a variable `baseUrl` (default `http://localhost:8081`). Change it if you run on another port.
4. Run the requests in order, starting with **Create Transaction**.

To test pagination, create at least 11 transactions with different `refId` values (`TXN0001` to `TXN0012`) inside postman, then call `?page=0` and `?page=1`.

### Test data

There is no seed script and no data is loaded automatically. The test records were **created manually in Postman** by sending `POST /api/transactions` requests, one per record. All records are dummy data (for example, merchant "Tesco" with `TXN0001`).

## Logging

Every request and response under `/api/**` is logged by `RequestResponseLoggingFilter`.

- Log file: `logs/cards-api.log`, created automatically in the project folder
- The log file from the test run is **included in this repository** under `logs/`, so the reviewer can see the request and response records without running the project
- Rotation: daily, keeping 30 days of history
- Each call produces two lines, one for the request and one for the response (status, time taken and body)

Example:

```
2026-10-22 10:40:01.123 INFO  - REQUEST  | POST /api/transactions | body={"refId":"TXN0001",...}
2026-10-22 10:40:01.180 INFO  - RESPONSE | POST /api/transactions | status=201 | 57ms | body={...}
```

## Transaction management

`CardTransactionService` uses Spring's `@Transactional`:

| Operation | Annotation |
|---|---|
| Create (INSERT) | `@Transactional` |
| Update | `@Transactional` |
| Get one and get list | `@Transactional(readOnly = true)` |

If an exception occurs inside a transactional method, the database changes are rolled back.

The currency conversion service is deliberately **not** transactional. The database read is a short transaction of its own, so no database connection is held open while waiting for the external API.

## Project structure

```
card-api-assignment/
├── pom.xml
├── README.md
├── postman/
│   └── cards-transaction.postman-collection.json
├── logs/                               (API request/response logs from the test run, included for review)
└── src/main/
    ├── java/com/bank/card_api_assignment/
    │   ├── CardApiAssignmentApplication.java
    │   ├── config/
    │   │   ├── DatabaseConnectionChecker.java   (prints DB connection details at startup)
    │   │   └── RestTemplateConfig.java          (HTTP client with timeouts)
    │   ├── controller/
    │   │   └── CardTransactionController.java   (REST endpoints)
    │   ├── dto/                                 (request and response objects)
    │   ├── entity/
    │   │   └── CardTransaction.java             (maps to the card_transactions table)
    │   ├── exception/                           (custom exceptions and global handler)
    │   ├── filter/
    │   │   └── RequestResponseLoggingFilter.java
    │   ├── repository/
    │   │   └── CardTransactionRepository.java
    │   └── service/
    │       ├── CardTransactionService.java      (business logic and @Transactional)
    │       └── CurrencyConversionService.java   (3rd-party API call)
    └── resources/
        ├── application.properties
        └── logback-spring.xml
```

| Layer | Responsibility |
|---|---|
| Controller | Receives HTTP requests and returns responses |
| Service | Business logic and transaction boundaries |
| Repository | Database access through Spring Data JPA |
| Entity | Table mapping |
| DTO | Request and response shapes, with validation rules |
| Exception | Consistent error handling through `@RestControllerAdvice` |
| Filter | Cross-cutting request and response logging |

## Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| `Login failed for user 'sa'` | Wrong password, or SQL authentication is not enabled. See Database setup. |
| `The TCP/IP connection to the host localhost, port 1433 has failed` | TCP/IP is not enabled or the service needs a restart. |
| `Cannot open database "TESTDB"` | Create the database: `CREATE DATABASE TESTDB;` |
| SSL or `PKIX path building failed` error | Keep `encrypt=true;trustServerCertificate=true` in the JDBC URL. |
| `Port 8081 was already in use` | Stop the other application, or change `server.port` in `application.properties`. |
| `Premature end of file` from Logback | `logback-spring.xml` is empty or malformed. Restore the content and run `./mvnw clean spring-boot:run`. |
| Lombok errors such as "cannot find symbol" on getters or setters | Enable the Lombok plugin and annotation processing in your IDE. |
| `./mvnw: Permission denied` or `mvnw` not found | Run it from the project root (the folder containing `pom.xml`). On Linux or macOS, run `chmod +x mvnw` first. |
| `Unknown lifecycle phase ".run"` or `spring-boot.run` not found | The goal is written with a colon: `spring-boot:run`, not `spring-boot.run`. |
| `502` from the convert endpoint | No internet access, or the exchange rate service is down. Retry later. |

## Assumptions and notes

- `ddl-auto=update` is used for convenience in this assignment. In production, schema changes would be managed with migrations (for example Flyway).
- `refId` is the business identifier and must be unique. `id` is the internal database key.
- Currency and status values are normalised to upper case before saving.
- All data is dummy test data. No real transactions or card details are used anywhere in this project.
- Credentials in `application.properties` are for a local development database only. In production they would come from environment variables or a secrets manager.
- Exchange rates come from a free public API and are for demonstration purposes.
