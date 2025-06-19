# Go Makefile for Lamp Control API

# Variables
GOCMD=go
GOBUILD=$(GOCMD) build
GOCLEAN=$(GOCMD) clean
GOTEST=$(GOCMD) test
GOGET=$(GOCMD) get
GOMOD=$(GOCMD) mod
GOFMT=gofmt
GOLINT=golangci-lint
BINARY_NAME=lamp-control-api
BINARY_PATH=./bin/$(BINARY_NAME)

# Default target
.PHONY: all
all: clean deps lint test build

# Install dependencies
.PHONY: deps
deps:
	$(GOMOD) download
	$(GOMOD) tidy

# Build the application
.PHONY: build
build:
	$(GOBUILD) -o $(BINARY_PATH) -v ./...

# Clean build artifacts
.PHONY: clean
clean:
	$(GOCLEAN)
	rm -f $(BINARY_PATH)

# Run tests
.PHONY: test
test:
	$(GOTEST) -v ./...

# Run tests with coverage
.PHONY: test-coverage
test-coverage:
	$(GOTEST) -v -coverprofile=coverage.out ./...
	$(GOCMD) tool cover -html=coverage.out -o coverage.html

# Linting targets
.PHONY: lint
lint: lint-golangci lint-fmt lint-vet

# Run golangci-lint
.PHONY: lint-golangci
lint-golangci:
	$(GOLINT) run

# Check formatting
.PHONY: lint-fmt
lint-fmt:
	@echo "Checking formatting..."
	@unformatted=$$($(GOFMT) -l .); \
	if [ -n "$$unformatted" ]; then \
		echo "The following files are not formatted:"; \
		echo "$$unformatted"; \
		exit 1; \
	fi

# Run go vet
.PHONY: lint-vet
lint-vet:
	$(GOCMD) vet ./...

# Fix formatting
.PHONY: fmt
fmt:
	$(GOFMT) -w .

# Install linting tools
.PHONY: install-lint-tools
install-lint-tools:
	@echo "Installing golangci-lint..."
	@which golangci-lint > /dev/null || curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/HEAD/install.sh | sh -s -- -b $(shell go env GOPATH)/bin v2.1.6

# Run the application
.PHONY: run
run: build
	$(BINARY_PATH)

# Development mode (watch for changes)
.PHONY: dev
dev:
	$(GOCMD) run main.go

# Security audit
.PHONY: audit
audit:
	$(GOCMD) list -json -m all | nancy sleuth

# Generate API code (if using code generation)
.PHONY: generate
generate:
	$(GOCMD) generate ./...

# Help target
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  all                - Clean, install deps, lint, test, and build"
	@echo "  deps               - Install dependencies"
	@echo "  build              - Build the application"
	@echo "  clean              - Clean build artifacts"
	@echo "  test               - Run tests"
	@echo "  test-coverage      - Run tests with coverage report"
	@echo "  lint               - Run all linting checks"
	@echo "  lint-golangci      - Run golangci-lint"
	@echo "  lint-fmt           - Check code formatting"
	@echo "  lint-vet           - Run go vet"
	@echo "  fmt                - Format code"
	@echo "  install-lint-tools - Install linting tools"
	@echo "  run                - Build and run the application"
	@echo "  dev                - Run in development mode"
	@echo "  audit              - Run security audit"
	@echo "  generate           - Generate code"
	@echo "  help               - Show this help message"
