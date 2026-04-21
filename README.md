# Rewards Program API

A Spring Boot REST API for calculating customer reward points based on transaction history. Customers earn points based on their purchase amounts: 2 points for every dollar spent over $100, and 1 point for every dollar spent between $50–$100 per transaction.

## Project Overview

This application implements a rewards calculation system for a retailer's loyalty program. It processes transaction data and calculates total reward points earned by customers over a specified period.

**Key Features:**
- Two REST endpoints: per-customer with optional date range, and all-customers for the last 3 months
- Thin controller — only mappings and exception handlers; all business logic in the service layer
- Dedicated `PointsCalculationService` for pure points calculation, independently testable
- Comprehensive monthly reward breakdown per customer
- Robust error handling and input validation
- Complete test coverage
- Detailed logging for troubleshooting

## Reward Calculation Logic

| Purchase Amount | Points Earned |
|----------------|--------------|
| Below $50 | 0 points |
| $50 – $100 | 1 point per dollar over $50 |
| Over $100 | 50 points (for $50–$100) + 2 points per dollar over $100 |

**Examples:**
- $45 → 0 points
- $75 → 25 points (1 × $25)
- $120 → 90 points (50 + 2 × $20)
- $200 → 250 points (50 + 2 × $100)

## Architecture

```
┌──────────────────────────────────────┐
│           RewardsController          │
│  (mappings + exception handlers only)│
└────────────────┬─────────────────────┘
                 │
       ┌─────────▼──────────┐
       │ RewardCalculation  │
       │     Service        │
       └──┬──────────┬──────┘
          │          │
  ┌───────▼───┐  ┌───▼──────────────┐
  │  Points   │  │  DataService     │
  │Calculation│  │  CustomerService │
  │  Service  │  └──────────────────┘
  └───────────┘
          │
  ┌───────▼──────────────────┐
  │  Models                  │
  │  - Transaction           │
  │  - Customer              │
  │  - RewardsResponse       │
  │    └─ MonthlyRewards     │
  └──────────────────────────┘
```

### Components

**RewardsController** — HTTP layer only
- Routes `GET /api/rewards/{customerId}` and `GET /api/v1/rewards/calculate`
- No business logic; delegates everything to `RewardCalculationService`
- Handles `CustomerNotFoundException` (404), `InvalidInputException` (400), and generic errors (500)

**RewardCalculationService** — orchestration
- `calculateRewards(customerId, startDate, endDate)` — parses string dates, applies defaults, validates, then delegates to core method
- `calculateRewardsForAllCustomers()` — computes last-3-months range and maps over all customers
- `calculateRewardsForCustomer(customerId, startDate, endDate)` — core: fetch, filter, aggregate, build response

**PointsCalculationService** — pure calculation
- Single responsibility: `calculatePoints(BigDecimal amount) → long`
- No dependencies; injected into `RewardCalculationService`

**DataService** — mock transaction store
- In-memory list of 18 transactions across 3 customers (Jan–Mar 2026)
- Each transaction has a unique sequential ID (1–18)
- Supports both synchronous and asynchronous retrieval

**CustomerService** — customer lookup
- In-memory store of 3 customers
- `getCustomer(id)` and `getAllCustomers()`

### Model Classes

| Class | Description |
|-------|-------------|
| `Transaction` | `id`, `customerId`, `amount`, `transactionDate` |
| `Customer` | `id`, `name`, `email` |
| `RewardsResponse` | Full response DTO including `MonthlyRewards` as a static inner class |
| `RewardsResponse.MonthlyRewards` | `month`, `transactionCount`, `totalSpent`, `rewardsEarned` |

## Technical Stack

| | |
|--|--|
| Framework | Spring Boot 4.0.5 |
| Java | 8 |
| Build | Maven |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Logging | SLF4J + Logback |
| JSON | Jackson (via Spring Boot) |

## API Reference

**Base URL:** `http://localhost:8082`

---

### GET /api/rewards/{customerId}

Returns reward points for a single customer over an optional date range.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `customerId` | Long | Yes | Customer identifier (1, 2, or 3) |

**Query Parameters**

| Parameter | Format | Required | Default |
|-----------|--------|----------|---------|
| `startDate` | YYYY-MM-DD | No | 90 days ago |
| `endDate` | YYYY-MM-DD | No | Today |

**Validation rules:**
- `startDate` must not be after `endDate`
- `endDate` must not be in the future

---

### GET /api/v1/rewards/calculate

Returns reward points for **all customers** over the last 3 calendar months. No input required.

**Date range:** First day of the month 3 months ago → today.  
Example (run on 2026-04-21): `2026-01-01` → `2026-04-21`

---

### Response Structure

Both endpoints return the same `RewardsResponse` shape (the calculate endpoint returns an array of them).

```json
{
  "customerId": 1,
  "customerName": "John Doe",
  "email": "john.doe@example.com",
  "queryStartDate": "2026-01-01",
  "queryEndDate": "2026-03-31",
  "transactionCount": 7,
  "totalPurchaseAmount": 705.75,
  "totalRewardsPoints": 530,
  "monthlyRewards": [
    {
      "month": "2026-01",
      "transactionCount": 3,
      "totalSpent": 240.50,
      "rewardsEarned": 180
    },
    {
      "month": "2026-02",
      "transactionCount": 2,
      "totalSpent": 260.00,
      "rewardsEarned": 220
    },
    {
      "month": "2026-03",
      "transactionCount": 2,
      "totalSpent": 205.25,
      "rewardsEarned": 130
    }
  ],
  "transactions": [
    {
      "id": 1,
      "customerId": 1,
      "amount": 120.00,
      "transactionDate": "2026-01-10"
    }
  ]
}
```

### Error Responses

**404 — Customer Not Found**
```json
{
  "errorCode": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found: 999",
  "statusCode": 404
}
```

**400 — Invalid Input**
```json
{
  "errorCode": "INVALID_INPUT",
  "message": "Start date cannot be after end date",
  "statusCode": 400
}
```

**500 — Internal Server Error**
```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "statusCode": 500
}
```

### curl Examples

```bash
# Single customer with explicit date range
curl "http://localhost:8082/api/rewards/1?startDate=2026-01-01&endDate=2026-03-31"

# Single customer, default date range (last 90 days)
curl "http://localhost:8082/api/rewards/2"

# All customers, last 3 months (no input required)
curl "http://localhost:8082/api/v1/rewards/calculate"
```

## Build and Run

**Prerequisites:** Java 8+, Maven 3.6+

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run the JAR directly
mvn clean package
java -jar target/assignments-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8082`.

## Testing

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=RewardsControllerTest
mvn test -Dtest=RewardCalculationServiceTest
mvn test -Dtest=DataServiceTest
```

### Test Coverage

**RewardsControllerTest** — integration tests via MockMvc
- Valid requests with and without date parameters
- Invalid customer ID → 404
- Invalid date format → 400
- Start date after end date → 400
- Future end date → 400
- Response structure and monthly breakdown

**RewardCalculationServiceTest** — service unit tests
- Points calculation for amounts below $50, between $50–$100, and above $100
- Edge cases: exactly $50, exactly $100, zero, null
- Customer not found → `CustomerNotFoundException`
- Invalid date range → `InvalidInputException`
- Null dates apply defaults correctly
- Monthly totals sum to overall total

**DataServiceTest** — data layer tests
- Transaction retrieval by customer ID
- Non-existent customer returns empty list
- All-transactions retrieval
- Async retrieval

## Project Structure

```
src/
├── main/
│   ├── java/com/infy/assignments/
│   │   ├── AssignmentsApplication.java
│   │   ├── controller/
│   │   │   └── RewardsController.java
│   │   ├── service/
│   │   │   ├── RewardCalculationService.java
│   │   │   ├── PointsCalculationService.java
│   │   │   ├── DataService.java
│   │   │   └── CustomerService.java
│   │   ├── model/
│   │   │   ├── Transaction.java
│   │   │   ├── Customer.java
│   │   │   └── RewardsResponse.java   (includes MonthlyRewards as inner class)
│   │   └── exception/
│   │       ├── CustomerNotFoundException.java
│   │       └── InvalidInputException.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/infy/assignments/
        ├── controller/
        │   └── RewardsControllerTest.java
        ├── service/
        │   ├── RewardCalculationServiceTest.java
        │   └── DataServiceTest.java
        └── AssignmentsApplicationTests.java
```

## Sample Data

18 mock transactions pre-loaded across 3 customers (IDs: 1, 2, 3), covering Jan–Mar 2026. All transactions have unique sequential IDs (1–18).

| Customer | Name | Transactions |
|----------|------|-------------|
| 1 | John Doe | 7 (Jan–Mar 2026) |
| 2 | Jane Smith | 6 (Jan–Mar 2026) |
| 3 | Bob Johnson | 5 (Jan–Mar 2026) |

## Troubleshooting

**Application won't start**
- Check Java version: `java -version` (requires 8+)
- Ensure port 8082 is free

**API returns 404**
- Valid customer IDs are `1`, `2`, and `3`

**API returns 400**
- Date format must be `YYYY-MM-DD`
- `startDate` must not be after `endDate`
- `endDate` must not be in the future

---

**Version:** 0.0.1-SNAPSHOT | **Java:** 8 | **Spring Boot:** 4.0.5 | **Last Updated:** 2026-04-21
