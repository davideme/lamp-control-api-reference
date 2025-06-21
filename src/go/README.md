# Go Implementation

This directory contains the Go implementation of the Lamp Control API using an in-memory map as storage.

## Features

- REST API with OpenAPI 3.0 specification
- In-memory storage using Go maps with thread-safe access
- Full CRUD operations for lamp management
- Generated API client/server code using oapi-codegen
- Comprehensive unit tests

## Architecture

The implementation uses:
- **Chi Router**: Lightweight HTTP router for handling REST endpoints
- **Map Storage**: In-memory map with `sync.RWMutex` for thread-safe operations
- **OpenAPI Integration**: Generated types and handlers from OpenAPI specification
- **Strict Handler Interface**: Type-safe request/response handling

## Storage Implementation

The lamp data is stored in a thread-safe map:
```go
type LampAPI struct {
    lamps map[string]Lamp  // In-memory storage
    mutex sync.RWMutex     // Thread-safe access
}
```

## API Endpoints

- `GET /lamps` - List all lamps
- `POST /lamps` - Create a new lamp  
- `GET /lamps/{lampId}` - Get a specific lamp
- `PUT /lamps/{lampId}` - Update a lamp's status
- `DELETE /lamps/{lampId}` - Delete a lamp

## Getting Started

### Prerequisites

- Go 1.24.3 or later
- golangci-lint (for linting)

### Installation

1. Install dependencies:
   ```bash
   go mod tidy
   ```

2. Install linting tools:
   ```bash
   make install-lint-tools
   ```

### Building and Running

1. **Build the application**:
   ```bash
   go build -o bin/lamp-control-api ./cmd/lamp-control-api
   ```

2. **Run the server**:
   ```bash
   ./bin/lamp-control-api --port=8080
   ```

3. **Run tests**:
   ```bash
   go test ./api/
   ```

### Testing the API

Once the server is running, you can test the endpoints:

1. **Create a lamp**:
   ```bash
   curl -X POST http://localhost:8080/lamps \
     -H "Content-Type: application/json" \
     -d '{"status": true}'
   ```

2. **List all lamps**:
   ```bash
   curl -X GET http://localhost:8080/lamps
   ```

3. **Get a specific lamp**:
   ```bash
   curl -X GET http://localhost:8080/lamps/{lampId}
   ```

4. **Update a lamp**:
   ```bash
   curl -X PUT http://localhost:8080/lamps/{lampId} \
     -H "Content-Type: application/json" \
     -d '{"status": false}'
   ```

5. **Delete a lamp**:
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
├── .vscode/        # VS Code settings
├── bin/            # Built binaries
├── api/            # API layer code
│   ├── lamp.go     # Main API implementation with map storage
│   ├── lamp_test.go # Unit tests
│   ├── lamp.gen.go # Generated OpenAPI code
│   └── cfg.yaml    # Code generation config
├── cmd/            # Application entry points
│   └── lamp-control-api/
│       └── main.go # Server main function
├── .golangci.yml   # golangci-lint configuration
├── Makefile        # Build and development tasks
├── go.mod          # Go module file
├── go.sum          # Go dependencies checksum
├── main.go         # Application entry point
└── README.md       # This file
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
