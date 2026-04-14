# Rewards Program API

A Spring Boot REST API for calculating customer reward points based on transaction history. Customers earn points based on their purchase amounts: 2 points for every dollar spent over $100, and 1 point for every dollar spent between $50-$100 per transaction.

## Project Overview

This application implements a rewards calculation system for a retailer's loyalty program. It processes transaction data and calculates total reward points earned by customers over a specified period (default: last 90 days).

**Key Features:**
- RESTful API endpoint to query customer rewards
- Dynamic date range support for flexible querying
- Comprehensive transaction and monthly reward breakdown
- Robust error handling and input validation
- Complete test coverage with 34 test cases
- Detailed logging for troubleshooting

## Design Details

### Reward Calculation Logic

The reward points calculation follows this formula:
- Amount < $50: 0 points
- Amount $50-$100: 1 point per dollar
- Amount > $100: 50 points (for $50-$100 range) + 2 points per dollar (for amount over $100)

**Example:**
- $45 purchase = 0 points
- $75 purchase = 25 points (1 × $25)
- $120 purchase = 90 points (50 + 40)
- $200 purchase = 250 points (50 + 200)

### System Architecture

```
┌─────────────────────────────────────────┐
│      REST Controller                    │
│   (RewardsController)                   │
└──────────────┬──────────────────────────┘
               │
               ├─────────────────────┬──────────────────────┐
               │                     │                      │
        ┌──────▼────┐        ┌──────▼───────┐    ┌──────────▼────┐
        │Calculation│        │Data Service  │    │Customer Svc   │
        │Service    │        │(Async Data)  │    │(Customer Data)│
        └───────────┘        └──────────────┘    └───────────────┘
               │
        ┌──────▼──────────┐
        │ Models/DTOs    │
        │ - Transaction  │
        │ - Customer     │
        │ - MonthlyRewards
        │ - RewardsResponse
        └───────────────┘
```

### Key Components

**RewardsController**: Handles HTTP requests and validates inputs
- Validates customer ID and date range
- Enforces business logic constraints (dates cannot be in future)
- Returns comprehensive reward information

**RewardCalculationService**: Core business logic
- Calculates points for individual transactions
- Groups transactions by month
- Aggregates monthly and total rewards
- Validates date ranges

**DataService**: Data access and async operations
- Provides mock transaction data
- Supports asynchronous data retrieval
- Contains sample data for 3 customers across 3 months

**CustomerService**: Customer lookup
- Manages customer master data
- Provides customer information by ID

## Technical Stack

- **Framework**: Spring Boot 4.0.5
- **Java Version**: Java 8
- **Build Tool**: Maven
- **Testing**: JUnit 5 (Jupiter), Spring Boot Test, MockMvc
- **Logging**: SLF4J with Logback
- **JSON Processing**: Jackson (comes with Spring)

## API Documentation

### Base URL
```
http://localhost:8080/api/rewards
```

### Endpoints

#### Get Customer Rewards
```
GET /api/rewards/{customerId}
```

**Path Parameters:**
- `customerId` (required): Customer identifier (e.g., CUST001)

**Query Parameters:**
- `startDate` (optional): Start date in YYYY-MM-DD format. Defaults to 90 days ago.
- `endDate` (optional): End date in YYYY-MM-DD format. Defaults to today.

**Response:** RewardsResponse containing:
- `customerId`: The queried customer ID
- `customerName`: Customer's full name
- `email`: Customer's email address
- `queryStartDate`: Start date used for calculation
- `queryEndDate`: End date used for calculation
- `transactionCount`: Total number of transactions in date range
- `totalPurchaseAmount`: Sum of all transaction amounts
- `totalRewardsPoints`: Total reward points earned
- `monthlyRewards`: Array of monthly breakdown:
  - `month`: Year-Month (e.g., "2026-01")
  - `transactionCount`: Transactions in that month
  - `totalSpent`: Amount spent in that month
  - `rewardsEarned`: Points earned in that month
- `transactions`: Array of individual transactions with details

### Request Examples

**Request 1: Query with explicit date range**
```bash
curl -X GET "http://localhost:8080/api/rewards/CUST001?startDate=2026-01-01&endDate=2026-03-31"
```

**Request 2: Default date range (last 90 days)**
```bash
curl -X GET "http://localhost:8080/api/rewards/CUST002"
```

**Request 3: Single month query**
```bash
curl -X GET "http://localhost:8080/api/rewards/CUST003?startDate=2026-02-01&endDate=2026-02-28"
```

### Response Example

```json
{
  "customerId": "CUST001",
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
      "id": "T00001",
      "customerId": "CUST001",
      "amount": 120.00,
      "transactionDate": "2026-01-10"
    }
  ]
}
```

### Error Responses

**404 - Customer Not Found**
```json
{
  "errorCode": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found: INVALID_ID",
  "statusCode": 404
}
```

**400 - Invalid Input**
```json
{
  "errorCode": "INVALID_INPUT",
  "message": "Start date cannot be after end date",
  "statusCode": 400
}
```

**500 - Internal Server Error**
```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "statusCode": 500
}
```

## Error Codes

| Code | HTTP Status | Meaning |
|------|-------------|---------|
| CUSTOMER_NOT_FOUND | 404 | Requested customer ID does not exist |
| INVALID_INPUT | 400 | Input validation failed (invalid dates, etc.) |
| INTERNAL_ERROR | 500 | Unexpected server error |

## How to Build and Run

### Prerequisites
- Java 8 or higher
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Alternative: Direct JAR Execution
```bash
mvn clean package
java -jar target/assignments-0.0.1-SNAPSHOT.jar
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=RewardCalculationServiceTest
mvn test -Dtest=RewardsControllerTest
mvn test -Dtest=DataServiceTest
```

### Test Results
```
Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
```

### Test Coverage

**RewardCalculationServiceTest (16 tests)**
- Point calculation for various transaction amounts
- Edge cases ($50.00, $100.00, $100.01)
- Zero and null amounts
- Customer lookup and not found scenario
- Date range validation
- Monthly grouping and aggregation
- Empty transaction scenarios

**RewardsControllerTest (11 tests)**
- Valid requests with date ranges
- Default date range handling
- Invalid customer ID (404)
- Invalid date formats (500)
- Invalid date ranges (400)
- Future date rejection (400)
- Response structure validation
- Multiple customer queries
- Transaction and monthly detail inclusion

**DataServiceTest (7 tests)**
- Transaction retrieval by customer
- Multiple customer queries
- Non-existent customer handling
- All transactions retrieval
- Asynchronous data retrieval
- Data integrity validation

## Sample Data

The application includes mock transaction data for 3 customers:

**Customer 1 (CUST001) - John Doe**
- 7 transactions across 3 months
- Total: $705.75, Rewards: 530 points

**Customer 2 (CUST002) - Jane Smith**
- 6 transactions across 3 months
- Transactions from 2026-01-05 to 2026-03-25

**Customer 3 (CUST003) - Bob Johnson**
- 5 transactions across 3 months
- Transactions from 2026-01-12 to 2026-03-12

## Code Quality Standards

✓ **Naming Convention**: Standardized camelCase for variables/methods, PascalCase for classes
✓ **Clean Code**: No console logs, removed in favor of SLF4J logging
✓ **Distinct Names**: Avoid variable name clashes between scopes
✓ **Proper Formatting**: Consistent indentation and code style
✓ **Documentation**: Javadoc comments on key methods
✓ **Input Validation**: All user inputs validated (customer ID, dates)
✓ **Exception Handling**: Custom exceptions with meaningful error messages
✓ **Logging**: DEBUG, INFO, WARN, and ERROR level logging throughout
✓ **Test Coverage**: Comprehensive test cases for multiple scenarios

## Exception Handling & Logging

### Logging Levels
- **DEBUG**: Detailed data retrieval operations
- **INFO**: Request processing and reward calculations
- **WARN**: Validation failures and edge cases
- **ERROR**: Exception details and stack traces

### Custom Exceptions
- `CustomerNotFoundException`: Thrown when customer ID not found
- `InvalidInputException`: Thrown for validation failures

Sample log output:
```
2026-04-15 00:17:46 [main] INFO  RewardsController - Received request for CUST001
2026-04-15 00:17:46 [main] INFO  RewardCalculationService - Calculating rewards from 2026-01-01 to 2026-03-31
2026-04-15 00:17:46 [main] DEBUG DataService - Fetching transactions for customer: CUST001
2026-04-15 00:17:46 [main] INFO  RewardCalculationService - Found 7 transactions
2026-04-15 00:17:46 [main] INFO  RewardCalculationService - Reward calculation complete. Total points: 530
```

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
│   │   │   ├── DataService.java
│   │   │   └── CustomerService.java
│   │   ├── model/
│   │   │   ├── Transaction.java
│   │   │   ├── Customer.java
│   │   │   ├── MonthlyRewards.java
│   │   │   └── RewardsResponse.java
│   │   └── exception/
│   │       ├── InvalidInputException.java
│   │       └── CustomerNotFoundException.java
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

## SCM Practices

This project follows proper Git practices:
- Meaningful commit messages describing changes
- Separate commits for logical features
- Clean commit history

## Future Enhancements

Potential improvements for future iterations:
- Database persistence (H2/PostgreSQL)
- JPA entities and repositories
- Transaction storage in database
- Customer master data management
- caching mechanisms for performance
- Batch processing for large datasets
- Additional query filters (transaction type, category)
- Excel/CSV export functionality
- Web UI for customer self-service

## Troubleshooting

### Application fails to start
- Verify Java 8+ is installed: `java -version`
- Check if port 8080 is available
- Review logs in console for explicit error messages

### Tests failing
- Ensure all source files are properly compiled: `mvn clean compile`
- Run `mvn test` to see detailed test output
- Check log files for asynchronous test timing issues

### API returning errors
- Verify customer ID exists (CUST001, CUST002, CUST003)
- Ensure date format is YYYY-MM-DD
- Check that startDate is not after endDate
- Confirm endDate is not in the future

## Support and Maintenance

For issues or enhancements, refer to the test cases for expected behavior.
All business logic is covered by comprehensive unit and integration tests.

---

**Version**: 0.0.1-SNAPSHOT
**Last Updated**: 2026-04-15
**Java Version**: 8
**Spring Boot Version**: 4.0.5
