# Credit Engine API

Loan simulation API developed with Spring Boot and Kotlin.

## 🚀 How to run

### Prerequisites
- Java 17+ (for local execution)
- Docker (for container execution)

### Running locally

```bash
./gradlew bootRun
```

### Running with Docker

#### Option 1: Docker Build + Run
```bash
# Build image
docker build -t credit-engine .

# Run container
docker run -p 8080:8080 credit-engine
```

#### Option 2: Docker Compose
```bash
docker-compose up --build
```

The application will be available at: `http://localhost:8080`

## 📚 API Documentation

After starting the application, access:

- **Swagger UI**: http://localhost:8080/swagger
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔧 Endpoints

### POST /simulations

Performs loan simulation based on amount, birthdate, and number of installments.

**Request example:**
```json
{
  "amount": 10000.00,
  "birthdate": "15/03/1990",
  "installments": 12
}
```

**Response example:**
```json
{
  "totalAmount": 10400.00,
  "installmentAmount": 866.67,
  "totalFee": 400.00
}
```

**Validations:**
- `amount`: Must be positive
- `birthdate`: Format dd/MM/yyyy
- `installments`: Must be positive integer

**Fee Calculation:**
- Age 18-25: 5% annual rate
- Age 26-40: 3% annual rate  
- Age 41-60: 2% annual rate
- Age 60+: 4% annual rate

### POST /simulations/batch

Performs batch loan simulations for multiple applications.

**Request example:**
```json
{
  "loanApplications": [
    {
      "amount": 10000.00,
      "birthdate": "15/03/1990",
      "installments": 12
    },
    {
      "amount": 5000.00,
      "birthdate": "20/05/1985",
      "installments": 24
    }
  ]
}
```

**Response example:**
```json
{
  "batchId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "totalSimulations": 2,
  "completedSimulations": 0,
  "failedSimulations": 0,
  "createdAt": "2024-01-15T10:30:00"
}
```

### GET /simulations/batch/{batchId}

Retrieves the status of a batch simulation.

**Response example:**
```json
{
  "batchId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "totalSimulations": 2,
  "completedSimulations": 2,
  "failedSimulations": 0,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Batch Status:**
- `PENDING`: Batch created, simulations not started
- `PROCESSING`: Simulations in progress
- `COMPLETED`: All simulations finished

### GET /simulations/successful

Retrieves successful simulations with pagination.

**Query Parameters:**
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20)

**Response example:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "batchId": "550e8400-e29b-41d4-a716-446655440000",
      "amountRequested": 10000.00,
      "birthdate": "1990-03-15",
      "installments": 12,
      "totalAmount": 10400.00,
      "installmentAmount": 866.67,
      "totalFee": 400.00,
      "processedAt": "2024-01-15T10:35:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### GET /simulations/batch/{batchId}/results

Retrieves successful simulations for a specific batch with pagination.

**Query Parameters:**
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20)

**Response example:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "batchId": "550e8400-e29b-41d4-a716-446655440000",
      "amountRequested": 10000.00,
      "birthdate": "1990-03-15",
      "installments": 12,
      "totalAmount": 10400.00,
      "installmentAmount": 866.67,
      "totalFee": 400.00,
      "processedAt": "2024-01-15T10:35:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Error Responses:**
- `404 Not Found`: When batch doesn't exist
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Server processing error

## 🧪 Tests

### Run all tests
```bash
./gradlew test
```

### Run specific test classes
```bash
./gradlew test --tests "*SimulationServiceTest"
./gradlew test --tests "*SimulationsControllerTest"
./gradlew test --tests "*BatchSimulationServiceTest"
```

### Test Coverage
- Unit tests for business logic (`SimulationService`, `FeeService`, `BatchSimulationService`)
- Integration tests for API endpoints (`SimulationsController`)
- Validation tests for request/response handling
- Pagination and batch processing tests

## 🔍 Code Quality

### Linting with Detekt
```bash
./gradlew detekt
```

### Auto-fix formatting issues
```bash
./gradlew detektFormat
```

## 🐳 Docker

### Useful commands

```bash
# Build image
docker build -t credit-engine .

# Run container
docker run -p 8080:8080 credit-engine

# Run in background
docker run -d -p 8080:8080 --name credit-engine-app credit-engine

# View logs
docker logs credit-engine-app

# Stop container
docker stop credit-engine-app
```

## 🛠️ Technologies

- **Kotlin** - Programming language
- **Spring Boot** - Web framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database for development
- **Spring Validation** - Data validation
- **SpringDoc OpenAPI** - API documentation
- **JUnit 5** - Unit testing
- **MockK** - Mocking for tests
- **Detekt** - Code analysis and formatting
- **Docker** - Containerization
- **Gradle** - Build tool

## 📁 Project Structure

The project follows Clean Architecture principles to ensure separation of concerns, scalability, and testability.

- **Domain layer**: business rules, independent of frameworks
- **Service layer**: orchestration and use case logic
- **Controller layer**: handles HTTP requests and responses
- **Infrastructure layer**: data persistence and external integrations

![Clean Architecture](clean-architecture.png)

### Key Components

- **SimulationService**: Core loan simulation logic
- **BatchSimulationService**: Batch processing and event handling
- **FeeService**: Age-based fee calculation
- **SimulationsController**: REST API endpoints
- **Event System**: Asynchronous batch processing

## 🚀 Getting Started

1. **Clone the repository**
2. **Run locally**: `./gradlew bootRun`
3. **Access Swagger**: http://localhost:8080/swagger
4. **Run tests**: `./gradlew test`
5. **Check code quality**: `./gradlew detekt`

## 📈 Features

- Individual loan simulations
- Batch loan processing
- Asynchronous simulation processing
- Paginated results
- Batch status tracking
- Age-based fee calculation
- Comprehensive validation
- Event-driven architecture
- Clean Architecture principles
- Full test coverage