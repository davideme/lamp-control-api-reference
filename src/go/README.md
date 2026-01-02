# Go Implementation

This directory contains the Go implementation of the Lamp Control API with support for both in-memory and PostgreSQL storage.

## Features

- REST API with OpenAPI 3.0 specification
- **Dual storage support**: In-memory maps or PostgreSQL database
- **Conditional PostgreSQL activation**: Automatically uses PostgreSQL when configured, falls back to in-memory otherwise
- Full CRUD operations for lamp management
- Generated API client/server code using oapi-codegen
- Type-safe database operations using sqlc
- Comprehensive unit and integration tests

## Architecture

The implementation uses:
- **Chi Router**: Lightweight HTTP router for handling REST endpoints
- **Repository Pattern**: Abstraction for storage operations with multiple implementations
- **OpenAPI Integration**: Generated types and handlers from OpenAPI specification
- **Strict Handler Interface**: Type-safe request/response handling
- **pgx/v5**: PostgreSQL driver with connection pooling
- **sqlc**: Type-safe SQL query generation

## Storage Implementations

### In-Memory Storage (Default)

Used when no PostgreSQL configuration is provided:
```go
type InMemoryLampRepository struct {
    lamps map[string]*entities.LampEntity
    mutex sync.RWMutex  // Thread-safe access
}
```

### PostgreSQL Storage

Activated automatically when connection parameters are set via environment variables:
```go
type PostgresLampRepository struct {
    pool    *pgxpool.Pool
    queries *queries.Queries  // sqlc-generated queries
}
```

The application detects PostgreSQL configuration from environment variables and:
1. Uses PostgreSQL if any connection parameters are set
2. Falls back to in-memory storage if connection fails or no parameters provided
3. Uses pgx library defaults (overrideable by environment variables)

## API Endpoints

- `GET /health` - Health check endpoint (returns service status)
- `GET /lamps` - List all lamps
- `POST /lamps` - Create a new lamp  
- `GET /lamps/{lampId}` - Get a specific lamp
- `PUT /lamps/{lampId}` - Update a lamp's status
- `DELETE /lamps/{lampId}` - Delete a lamp

## Getting Started

### Prerequisites

- Go 1.24.3 or later
- golangci-lint (for linting)
- PostgreSQL 13+ (optional, for database storage)
- sqlc (for regenerating database queries, if needed)

### Installation

1. Install dependencies:
   ```bash
   go mod tidy
   ```

2. Install linting tools:
   ```bash
   make install-lint-tools
   ```

3. (Optional) Install sqlc for query generation:
   ```bash
   go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest
   ```

### PostgreSQL Configuration

The application supports PostgreSQL storage through environment variables. If any connection parameters are set, PostgreSQL will be used; otherwise, the application falls back to in-memory storage.

#### Environment Variables

- `DATABASE_URL` - Full PostgreSQL connection string (takes precedence over individual parameters)
- `DB_HOST` - PostgreSQL host (default: `localhost`)
- `DB_PORT` - PostgreSQL port (default: `5432`)
- `DB_NAME` - Database name (default: `postgres`)
- `DB_USER` - Database username (default: current system user)
- `DB_PASSWORD` - Database password (default: empty)
- `DB_POOL_MIN_SIZE` - Minimum connection pool size (default: `0`)
- `DB_POOL_MAX_SIZE` - Maximum connection pool size (default: `4`)

#### Database Setup

1. **Using Docker Compose** (recommended for local development):
   ```bash
   # From repository root
   docker-compose up postgres
   ```

2. **Initialize the database schema**:
   ```bash
   # From src/go directory
   psql -h localhost -U lamp_user -d lamp_control -f ../../database/sql/postgresql/schema.sql
   ```
   Default credentials from docker-compose:
   - Host: `localhost`
   - Port: `5432`
   - Database: `lamp_control`
   - User: `lamp_user`
   - Password: `lamp_password`

3. **Run with PostgreSQL**:
   ```bash
   DB_HOST=localhost DB_PORT=5432 DB_NAME=lamp_control \
   DB_USER=lamp_user DB_PASSWORD=lamp_password \
   ./bin/lamp-control-api --port=8080
   ```

#### Connection String Examples

Using individual parameters:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=lamp_control
export DB_USER=lamp_user
export DB_PASSWORD=lamp_password
```

Using DATABASE_URL:
```bash
# Development only - disables SSL. For production, use sslmode=require or sslmode=verify-full
export DATABASE_URL="postgres://lamp_user:lamp_password@localhost:5432/lamp_control?sslmode=disable"
```

### Building and Running

1. **Build the application**:
   ```bash
   go build -o bin/lamp-control-api ./cmd/lamp-control-api
   ```

2. **Run the server (in-memory mode)**:
   ```bash
   ./bin/lamp-control-api --port=8080
   ```

3. **Run the server (PostgreSQL mode)**:
   ```bash
   DB_HOST=localhost DB_USER=lamp_user DB_PASSWORD=lamp_password \
   ./bin/lamp-control-api --port=8080
   ```

4. **Run tests with coverage**:
   ```bash
   make test-coverage
   ```

5. **Check coverage meets minimum threshold (80%)**:
   ```bash
   make coverage-check
   ```

5. **View coverage report**:
   ```bash
   make coverage-report
   ```

### Coverage Requirements

The project enforces a minimum code coverage of **80%** in CI/CD pipeline:
- Generated files (`.gen.go`) are excluded from coverage calculation
- Main packages are excluded since they're difficult to unit test
- Coverage is checked on every PR and push to main/develop branches
- Local coverage checking available via `make coverage-check`

### Testing

#### Unit Tests (In-Memory)

Standard unit tests run without PostgreSQL:
```bash
go test ./... -v
```

#### Integration Tests (PostgreSQL)

PostgreSQL integration tests automatically skip if no database is configured:

```bash
# Set up PostgreSQL using docker-compose
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
sleep 5

# Initialize schema
psql -h localhost -U lamp_user -d lamp_control -f ../../../database/sql/postgresql/schema.sql

# Run tests with PostgreSQL
DB_HOST=localhost DB_PORT=5432 DB_NAME=lamp_control \
DB_USER=lamp_user DB_PASSWORD=lamp_password \
go test ./... -v

# Clean up
docker-compose down
```

The PostgreSQL tests will:
- **Skip gracefully** if no PostgreSQL configuration is detected
- **Test all CRUD operations** against a real PostgreSQL database
- **Clean up after themselves** by deleting test data

### Testing the API

Once the server is running, you can test the endpoints:

1. **Check service health**:
   ```bash
   curl -X GET http://localhost:8080/health
   ```
   
   Expected response:
   ```json
   {"status":"ok"}
   ```

2. **Create a lamp**:
   ```bash
   curl -X POST http://localhost:8080/lamps \
     -H "Content-Type: application/json" \
     -d '{"status": true}'
   ```

3. **List all lamps**:
   ```bash
   curl -X GET http://localhost:8080/lamps
   ```

4. **Get a specific lamp**:
   ```bash
   curl -X GET http://localhost:8080/lamps/{lampId}
   ```

5. **Update a lamp**:
   ```bash
   curl -X PUT http://localhost:8080/lamps/{lampId} \
     -H "Content-Type: application/json" \
     -d '{"status": false}'
   ```

6. **Delete a lamp**:
   ```bash
   curl -X DELETE http://localhost:8080/lamps/{lampId}
   ```

## Development Workflow

1. **Format code**: `make fmt`
2. **Run linting**: `make lint`
3. **Run tests**: `make test`
4. **Build application**: `make build`
5. **Run all checks**: `make all`

## Directory Structure

```
go/
├── .vscode/              # VS Code settings
├── bin/                  # Built binaries
├── api/                  # API layer code
│   ├── entities/         # Domain entities
│   │   └── lamp_entity.go
│   ├── queries/          # sqlc-generated database code
│   │   ├── db.go
│   │   ├── models.go
│   │   ├── querier.go
│   │   └── lamps.sql.go
│   ├── lamp.go           # Main API implementation
│   ├── lamp_mapper.go    # Entity/model conversion
│   ├── repository.go     # Repository interface and in-memory impl
│   ├── postgres_repository.go  # PostgreSQL repository impl
│   ├── config.go         # Database configuration
│   ├── lamp_test.go      # Unit tests
│   ├── postgres_repository_test.go  # Integration tests
│   ├── lamp.gen.go       # Generated OpenAPI code
│   └── cfg.yaml          # Code generation config
├── cmd/                  # Application entry points
│   └── lamp-control-api/
│       └── main.go       # Server main function
├── .golangci.yml         # golangci-lint configuration
├── sqlc.yaml             # sqlc configuration
├── Makefile              # Build and development tasks
├── go.mod                # Go module file
├── go.sum                # Go dependencies checksum
└── README.md             # This file
```

## Code Quality

This project uses several tools to maintain code quality:

### Linting
- **golangci-lint**: Comprehensive Go linter with multiple analyzers
- **go vet**: Go's built-in static analyzer
- **gofmt**: Code formatting checker

### Available Make Targets
- `make lint` - Run all linting checks
- `make lint-golangci` - Run golangci-lint only
- `make lint-fmt` - Check code formatting
- `make lint-vet` - Run go vet
- `make fmt` - Format code automatically
- `make test` - Run tests
- `make test-coverage` - Run tests with coverage report
- `make build` - Build the application
- `make all` - Run complete CI pipeline (clean, deps, lint, test, build)

### Pre-commit Hook
A pre-commit hook is available at `.pre-commit-hook` that runs:
- Code formatting check
- go vet
- golangci-lint (if available)
- Build verification
- Test execution

To install the pre-commit hook:
```bash
cp .pre-commit-hook ../.git/hooks/pre-commit
chmod +x ../.git/hooks/pre-commit
```

### CI/CD
The project includes a GitHub Actions workflow (`.github/workflows/go-ci.yml`) that runs:
- Linting with golangci-lint
- Tests across multiple Go versions
- Security scanning with Gosec
- Build verification

## Implementation Details

### PostgreSQL Support

This implementation follows the requirements specified in [ADR 005: PostgreSQL Storage Support](../../docs/adr/005-postgresql-storage-support.md):

1. **No Migrations**: Uses the existing schema at `database/sql/postgresql/schema.sql` without migration tools
2. **Library Defaults**: Uses pgx/v5 library defaults, overrideable by environment variables
3. **Conditional Activation**: PostgreSQL is only activated when connection parameters are provided

### Design Decisions

#### Repository Pattern
The implementation uses the repository pattern to abstract storage operations:
- `LampRepository` interface defines the contract
- `InMemoryLampRepository` provides thread-safe in-memory storage
- `PostgresLampRepository` provides PostgreSQL storage with connection pooling

#### Type-Safe Queries
Uses sqlc to generate type-safe database code:
- Queries defined in `api/queries/lamps.sql`
- Generated code in `api/queries/` directory
- Compile-time type safety for SQL operations

#### Soft Deletes
The PostgreSQL implementation uses soft deletes (setting `deleted_at` timestamp) rather than hard deletes, allowing for data recovery and audit trails.

#### Environment Variable Precedence
1. `DATABASE_URL` takes precedence if set
2. Individual parameters (`DB_HOST`, `DB_PORT`, etc.) used if no `DATABASE_URL`
3. pgx library defaults used for any unspecified parameters
4. Falls back to in-memory storage if no parameters are set

#### Connection Pooling
The PostgreSQL implementation uses pgxpool with:
- Configurable min/max pool size via environment variables
- Health checks every minute
- Maximum connection lifetime of 1 hour
- Maximum idle time of 30 minutes

### Future Enhancements

Possible improvements for future iterations:
- Cursor-based pagination for large result sets
- Read replicas support for scaling reads
- Metrics and observability for database operations
- Migration tool integration (e.g., golang-migrate)
- Transaction support for complex operations
