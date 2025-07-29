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

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔧 Endpoints

### POST /simulations

Performs loan simulation.

**Request example:**
```json
{
  "amount": 10000.0,
  "birthdate": "15/03/1990",
  "installments": 12
}
```

**Validations:**
- `amount`: Must be positive
- `birthdate`: Format dd/MM/yyyy
- `installments`: Must be positive

## 🧪 Tests

```bash
./gradlew test
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
- **Spring Boot 3.x** - Web framework
- **Spring Validation** - Data validation
- **SpringDoc OpenAPI** - API documentation
- **JUnit 5** - Unit testing
- **MockK** - Mocking for tests
- **Docker** - Containerization

## 📁 Project structure