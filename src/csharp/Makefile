# Makefile for C# Lamp Control API
# Provides common tasks for building, testing, linting, and formatting

.PHONY: help build clean restore test test-coverage coverage format lint check install run

# Default target
help:
	@echo "Available targets:"
	@echo "  help     - Show this help message"
	@echo "  install  - Restore NuGet packages"
	@echo "  build    - Build the application"
	@echo "  clean    - Clean build artifacts"
	@echo "  test     - Run tests"
	@echo "  test-coverage - Run tests with coverage generation"
	@echo "  format   - Format code using dotnet format"
	@echo "  lint     - Run static analysis and linting"
	@echo "  check    - Run format check without applying changes"
	@echo "  run      - Run the application"
	@echo ""
	@echo "Formatting and Linting:"
	@echo "  format-check  - Check if code is properly formatted"
	@echo "  format-fix    - Apply formatting fixes"
	@echo "  lint-check    - Check for linting issues"

# Project settings
PROJECT_DIR = LampControlApi
PROJECT_FILE = $(PROJECT_DIR)/LampControlApi.csproj

# Install/restore dependencies
install restore:
	dotnet restore $(PROJECT_FILE)

# Build the application
build: restore
	dotnet build $(PROJECT_FILE) --no-restore --configuration Release

# Clean build artifacts
clean:
	dotnet clean $(PROJECT_FILE)
	rm -rf $(PROJECT_DIR)/bin $(PROJECT_DIR)/obj

# Run tests
test: build
	dotnet test $(PROJECT_FILE)

# Run tests with coverage
test-coverage coverage: build
	@echo "Running tests with coverage collection..."
	# Clean previous coverage results
	rm -rf TestResults coverage/latest
	# Run tests with coverage collection
	dotnet test $(PROJECT_FILE) --collect:"XPlat Code Coverage" --results-directory TestResults --settings coverlet.runsettings || \
	dotnet test $(PROJECT_FILE) --collect:"XPlat Code Coverage" --results-directory TestResults
	# Try to consolidate coverage files to a standard location
	@if [ -d TestResults ]; then \
		mkdir -p coverage/latest; \
		find TestResults -name "coverage.cobertura.xml" -exec cp {} TestResults/ \; 2>/dev/null || true; \
		find TestResults -name "*.xml" -path "*/coverage.cobertura.xml" -exec cp {} coverage/latest/ \; 2>/dev/null || true; \
		echo "Coverage files generated in TestResults/ and coverage/latest/"; \
		ls -la TestResults/ 2>/dev/null || echo "No TestResults directory found"; \
	fi

# Format code
format format-fix:
	dotnet format $(PROJECT_FILE) --include-generated

# Check formatting without applying changes
format-check check:
	dotnet format $(PROJECT_FILE) --verify-no-changes --verbosity diagnostic

# Run static analysis and linting
lint lint-check:
	dotnet build $(PROJECT_FILE) --verbosity normal --configuration Release

# Run the application
run: build
	cd $(PROJECT_DIR) && dotnet run

# Development workflow
dev: format lint build

# CI/CD workflow
ci: format-check lint test build
