# Go Implementation

This directory contains the Go implementation of the Lamp Control API.

## Features

- REST API (Gin/Echo with OpenAPI 3.0)
- GraphQL (gqlgen)
- gRPC
- Database support for MySQL, PostgreSQL, and MongoDB

## Getting Started

### Prerequisites

- Go 1.24.3 or later
- golangci-lint (for linting)

### Installation

1. Install dependencies:
   ```bash
   make deps
   ```

2. Install linting tools:
   ```bash
   make install-lint-tools
   ```

### Development Workflow

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
├── .golangci.yml   # golangci-lint configuration
├── .pre-commit-hook # Git pre-commit hook
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
