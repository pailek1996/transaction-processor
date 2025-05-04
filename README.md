# Transaction Processor API

A Spring Boot application that processes financial transactions with batch import functionality and secure API endpoints.

## Overview

This application provides:
- Transaction management (Create, Read, Update)
- Batch import of transactions from pipe-delimited files
- JWT-based authentication
- H2 in-memory database
- RESTful API design

## Tech Stack

- **Java 11**
- **Spring Boot 2.7.5**
- **Spring Security**
- **Spring Batch**
- **Spring Data JPA**
- **H2 Database**
- **Lombok**
- **JWT (JSON Web Tokens)**

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Your favorite IDE (IntelliJ IDEA recommended)

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd transaction-processor
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "password"
}

Response:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```

### Transaction Management

#### Create Transaction
```
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
    "accountNumber": "8872838283",
    "trxAmount": 100.00,
    "description": "FUND TRANSFER",
    "trxDate": "2023-01-01",
    "trxTime": "10:30:00",
    "customerId": "222"
}
```

#### Get All Transactions
```
GET /api/transactions
Authorization: Bearer {token}
```

#### Get Transaction by ID
```
GET /api/transactions/{id}
Authorization: Bearer {token}
```

#### Update Transaction
```
PUT /api/transactions/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
    "accountNumber": "8872838283",
    "trxAmount": 150.00,
    "description": "UPDATED TRANSFER",
    "trxDate": "2023-01-01",
    "trxTime": "10:30:00",
    "customerId": "222"
}
```

### Batch Import

#### Import Transactions from File
```
POST /api/batch/import?file={file-path}
Authorization: Bearer {token}

Example: POST /api/batch/import?file=C:/Users/username/dataSource.txt
```

## Batch File Format

The batch import expects a pipe-delimited (|) file with a header row. Format:

```
ACCOUNT_NUMBER|TRX_AMOUNT|DESCRIPTION|TRX_DATE|TRX_TIME|CUSTOMER_ID
8872838283|100.00|FUND TRANSFER|2023-01-01|10:30:00|222
8872838283|200.00|BILL PAYMENT|2023-01-02|14:45:00|222
```

**Important Notes:**
- Date format: `yyyy-MM-dd`
- Time format: `HH:mm:ss`
- Amount should be numeric values
- File must have a header row (it will be skipped)

## Database Access

H2 Console is available at: `http://localhost:8080/h2-console`

Use these credentials:
- JDBC URL: `jdbc:h2:mem:transactiondb`
- Username: `sa`
- Password: (leave empty)

## Default Credentials

- Username: `admin`
- Password: `password`

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Security

The application uses JWT for authentication. Include the token in the Authorization header:
```
Authorization: Bearer {token}
```

## Testing with Postman

Import the provided Postman collection and environment for easy API testing.

## Troubleshooting

### Common Issues

1. **File Not Found Error**: Ensure the file path in batch import is correct and accessible
2. **Date/Time Parsing Error**: Verify the file format matches the expected pattern
3. **Authentication Failed**: Check if JWT token is valid and properly included in headers

## Acknowledgments

Built with Spring Boot