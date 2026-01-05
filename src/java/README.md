
# Lamp Control API - Java

## Overview
This is a Java Spring Boot implementation of the Lamp Control API using the [OpenAPI Generator](https://openapi-generator.tech) project.
By using the [OpenAPI-Spec](https://openapis.org), you can easily generate an API stub.
This is an example of building API stub interfaces in Java using the Spring framework.

The stubs generated can be used in your existing Spring-MVC or Spring-Boot application to create controller endpoints
by adding ```@Controller``` classes that implement the interface. Eg:
```java
@Controller
public class LampController implements LampsApi {
// implement all LampsApi methods
}
```

You can also use the interface to create [Spring-Cloud Feign clients](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign-inheritance).Eg:
```java
@FeignClient(name="lamps", url="http://lamp-service.example.com/v1")
public interface LampClient extends LampsApi {

}
```

## Building and Running

### Prerequisites
- Java 21 (Eclipse Temurin recommended)
- Maven 3.6+

### Local Development
```bash
# Compile and run tests
mvn clean test

# Run the application
mvn spring-boot:run

# Package the application
mvn clean package
```

The application will be available at `http://localhost:8080/v1/lamps`

### Docker Multi-Stage Build

This project includes a multi-stage Dockerfile that uses OpenJDK 21 for building and Google's distroless image for runtime, following security best practices.

#### Build Docker Image
```bash
docker build -t lamp-control-api .
```

#### Run Docker Container
```bash
docker run -p 8080:8080 lamp-control-api
```

The Docker image uses:
- **Build stage**: `maven:3.9-eclipse-temurin-21` - Contains Maven and Eclipse Temurin JDK 21 for building the application
- **Runtime stage**: `gcr.io/distroless/java21-debian12` - Minimal, secure base image with only Java runtime

#### Multi-Stage Benefits
- **Security**: Distroless images have minimal attack surface
- **Size**: Smaller runtime image (no build tools, package managers)
- **Performance**: Faster container startup and reduced resource usage

## API Documentation

Once running, the API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Health Endpoint

The service provides a health check endpoint for monitoring and liveness probes:

- **GET** `/v1/health` - Returns service health status
  - Response: `{ "status": "ok" }` with HTTP 200 OK
  - Content-Type: `application/json`

This endpoint is useful for:
- Container orchestration health checks (Docker, Kubernetes)
- Load balancer health probes
- Monitoring systems
- CI/CD pipeline validation

## Database Configuration

This application supports PostgreSQL for persistent data storage using Spring Data JPA with Hibernate and HikariCP connection pooling.

### Configuration Options

The application can run in two modes:

1. **In-Memory Mode** (default when no database is configured): Uses a ConcurrentHashMap for storing lamp entities in memory
2. **PostgreSQL Mode**: Uses PostgreSQL database for durable storage

### PostgreSQL Setup

#### 1. Using Docker Compose

The easiest way to run PostgreSQL locally is using Docker Compose (from the repository root):

```bash
docker-compose up -d postgres
```

This will start a PostgreSQL instance on `localhost:5432`. Check the docker-compose.yml file for the exact database name, username, and password configured for your environment.

#### 2. Manual PostgreSQL Setup

If you prefer to use an existing PostgreSQL instance:

1. Create the database:
```sql
CREATE DATABASE lampcontrol;
CREATE USER your_username WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE lampcontrol TO your_username;
```

2. The schema will be automatically created by Flyway on application startup using the migration in `src/main/resources/db/migration/V1__Initial_schema.sql`

#### 3. Environment Variables

**IMPORTANT**: The default credentials in application.properties are intentionally set to "CHANGE_ME" to prevent accidental deployment with weak credentials. You MUST configure proper credentials before running the application.

Configure database connection using environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/lampcontrol
export DB_USER=your_username
export DB_PASSWORD=your_secure_password
export DB_POOL_MAX_SIZE=20
export DB_POOL_MIN_SIZE=5
```

Or create an `application-local.properties` file (add it to .gitignore):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lampcontrol
spring.datasource.username=your_username
spring.datasource.password=your_secure_password
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

#### 4. Running with PostgreSQL

```bash
# Run with PostgreSQL
mvn spring-boot:run

# Or with environment variables
DATABASE_URL=jdbc:postgresql://localhost:5432/lampcontrol \
DB_USER=lampuser \
DB_PASSWORD=lamppass \
mvn spring-boot:run
```

### Database Migration

The application uses [Flyway](https://flywaydb.org/) for database schema management. Migrations are automatically executed on startup:

- **Location**: `src/main/resources/db/migration/`
- **Initial Schema**: `V1__Initial_schema.sql`

Flyway will:
- Create the `lamps` table with appropriate indexes
- Set up triggers for automatic timestamp updates
- Enable UUID generation extension

To disable Flyway migrations:
```properties
spring.flyway.enabled=false
```

### Connection Pool Configuration

HikariCP is configured with production-ready defaults:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.pool-name=LampControlHikariCP
```

Adjust these values based on your load requirements.

### Testing with PostgreSQL

Integration tests use [Testcontainers](https://www.testcontainers.org/) to automatically spin up PostgreSQL containers:

```bash
# Run integration tests (includes PostgreSQL tests)
mvn test -P integration-tests

# Run only unit tests (no database required)
mvn test -P unit-tests

# Run all tests
mvn test -P all-tests
```

Testcontainers requirements:
- Docker installed and running
- Internet connection (to pull postgres:16-alpine image on first run)

### Troubleshooting

**Issue**: Application fails to start with "Failed to determine a suitable driver class"

**Solution**: This means PostgreSQL configuration is incomplete. Either:
- Remove/comment out `spring.datasource.url` to use in-memory mode
- Provide complete database configuration (URL, username, password)

**Issue**: Flyway migration fails

**Solution**: 
- Check PostgreSQL is running: `docker ps` or `pg_isready`
- Verify connection details in application.properties
- Check database user has sufficient privileges
- Review Flyway logs for specific error messages

