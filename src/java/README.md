
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

## Architecture

### Service Layer

This application follows a layered architecture pattern:

```
Controllers → LampService → LampRepository (interface)
                                 ↓
                    ┌────────────┴─────────────┐
                    ↓                          ↓
         InMemoryLampRepository    JpaLampRepository → Hibernate → PostgreSQL
            (default)                  (when DATABASE_URL is set)
```

**LampService** (`org.openapitools.service.LampService`):
- Provides business logic and transaction management
- Decorated with `@Transactional` for declarative transaction control
- Implements soft delete operations
- Handles entity-to-DTO mapping via LampMapper
- Supports pagination and custom queries

**Benefits**:
- Clear separation of concerns
- Centralized transaction management
- Easier testing with mocked dependencies
- Business logic isolated from HTTP concerns

## Database Configuration

This application supports two storage modes:

### Configuration Options

**By default, the application uses an in-memory repository** - no database setup required. This is ideal for:
- Local development and testing
- Quick demos and prototyping
- Environments where persistence is not needed

The application automatically switches to PostgreSQL mode when you provide a `DATABASE_URL` environment variable:

1. **In-Memory Mode** (default): Uses a ConcurrentHashMap for storing lamp entities in memory
   - Activated when: No `DATABASE_URL` is set
   - Data is lost on application restart
   - No database installation needed

2. **PostgreSQL Mode**: Uses PostgreSQL database for durable storage
   - Activated when: `DATABASE_URL` environment variable is set
   - Provides persistence across restarts
   - Requires PostgreSQL database

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

Configure database connection using environment variables to enable PostgreSQL mode:

```bash
# Required to enable PostgreSQL mode
# Accepted formats: jdbc:postgresql://..., postgresql://..., postgres://...
export DATABASE_URL=postgresql://localhost:5432/lampcontrol

# Optional override (takes precedence and is used as-is without normalization)
# export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lampcontrol

# Database credentials
export DB_USER=lampuser
export DB_PASSWORD=your_secure_password

# Enable Flyway migrations (required for PostgreSQL mode)
export FLYWAY_ENABLED=true

# Optional: Connection pool tuning
export DB_POOL_MAX_SIZE=20
export DB_POOL_MIN_SIZE=5
```

Or create an `application-local.properties` file (add it to .gitignore):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lampcontrol
spring.datasource.username=lampuser
spring.datasource.password=your_secure_password
spring.flyway.enabled=true
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

#### 4. Running the Application

```bash
# Run in-memory mode (default - no setup needed)
mvn spring-boot:run

# Run with PostgreSQL (requires DATABASE_URL)
DATABASE_URL=postgresql://localhost:5432/lampcontrol \
FLYWAY_ENABLED=true \
DB_USER=lampuser \
DB_PASSWORD=lamppass \
mvn spring-boot:run
```

### Database Migration

The application uses [Flyway](https://flywaydb.org/) for database schema management. Migrations are automatically executed on startup:

- **Location**: `src/main/resources/db/migration/`
- **Initial Schema**: `V1__Initial_schema.sql` - Creates lamps table with basic columns
- **Soft Deletes**: `V2__Add_soft_deletes.sql` - Adds deleted_at column for soft delete support

Flyway will:
- Create the `lamps` table with appropriate indexes
- Add soft delete support with `deleted_at` column
- Enable UUID generation extension

To disable Flyway migrations:
```properties
spring.flyway.enabled=false
```

### Soft Delete Behavior

The application implements soft deletes for lamp entities:

- **DELETE** requests set the `deleted_at` timestamp instead of removing records
- Soft-deleted lamps are automatically filtered from all queries using Hibernate's `@Where` clause
- Soft-deleted lamps do NOT appear in list operations or lookups
- Database retains historical data for audit purposes

This ensures data integrity while providing a logical deletion mechanism.

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

**Test Coverage**:
- **LampServiceTest** - Unit tests for service layer with mocked repository
- **JpaLampRepositoryIntegrationTest** - Integration tests with real PostgreSQL database
  - Includes soft delete tests
  - Custom query method tests (findByStatus, findAllActive, countActive)
  - Pagination tests

Testcontainers requirements:
- Docker installed and running
- Internet connection (to pull postgres:16-alpine image on first run)

## Testing & Code Coverage

### Running Tests

The project uses Maven profiles to separate test types:

```bash
# Run unit tests only (default profile, no Docker required)
cd src/java
mvn test

# Run integration tests only (requires Docker for Testcontainers)
mvn test -P integration-tests

# Run performance tests only
mvn test -P performance-tests

# Run all tests (unit + integration + performance)
mvn test -P all-tests
```

### Generating Code Coverage with JaCoCo

[JaCoCo](https://www.jacoco.org/) is configured in `pom.xml` and runs automatically during the `test` phase. It generates coverage reports in HTML, XML, and CSV formats.

```bash
# Run tests and generate coverage report (single command)
cd src/java
mvn test jacoco:report
```

This will:
1. Instrument the code via `jacoco:prepare-agent` (runs automatically)
2. Execute the tests
3. Enforce coverage thresholds (fails the build if not met)
4. Generate the report in `target/site/jacoco/`

#### Coverage Output Files

| File | Path | Description |
|------|------|-------------|
| HTML report | `target/site/jacoco/index.html` | Interactive browsable report |
| XML report | `target/site/jacoco/jacoco.xml` | Machine-readable (used by CI) |
| CSV report | `target/site/jacoco/jacoco.csv` | Spreadsheet-compatible data |

Open the HTML report in a browser:

```bash
open target/site/jacoco/index.html    # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

#### Coverage Thresholds

The build enforces minimum coverage ratios and will **fail** if not met:

| Counter | Minimum | Description |
|---------|---------|-------------|
| LINE | 80% | Line coverage |
| BRANCH | 75% | Branch coverage |

#### Excluded from Coverage

The following packages/classes are excluded from coverage analysis (generated or configuration code):

- `org.openapitools.api.**` (generated OpenAPI interfaces)
- `org.openapitools.model.**` (generated OpenAPI models)
- `OpenApiGeneratorApplication` (main entry point)
- `ApplicationMode` (enum)
- `config/FlywayConfig`, `config/DataSourceConfig`, `config/JpaConfig` (Spring config)

#### Coverage with Specific Test Profiles

```bash
# Coverage for unit tests only
mvn test jacoco:report -P unit-tests

# Coverage for integration tests only (requires Docker)
mvn test jacoco:report -P integration-tests

# Coverage for all tests combined
mvn test jacoco:report -P all-tests
```

#### CI Usage

The CI workflow (`.github/workflows/java-ci.yml`) runs:

```bash
mvn test jacoco:report
```

This generates the XML report at `target/site/jacoco/jacoco.xml` which is used for coverage tracking.

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

