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
TEST_PROJECT_DIR = LampControlApi.Tests
TEST_PROJECT_FILE = $(TEST_PROJECT_DIR)/LampControlApi.Tests.csproj
SOLUTION_FILE = LampControlApi.sln

# Install/restore dependencies
install restore:
	dotnet restore $(SOLUTION_FILE)

# Build the application
build: restore
	dotnet build $(SOLUTION_FILE) --no-restore --configuration Release

# Clean build artifacts
clean:
	dotnet clean $(SOLUTION_FILE)
	rm -rf $(PROJECT_DIR)/bin $(PROJECT_DIR)/obj $(TEST_PROJECT_DIR)/bin $(TEST_PROJECT_DIR)/obj TestResults

# Run tests
test: build
	dotnet test $(TEST_PROJECT_FILE) --no-build --configuration Release

# Run tests with coverage (Coverlet via coverlet.runsettings)
test-coverage coverage: build
	rm -rf TestResults
	dotnet test $(TEST_PROJECT_FILE) --no-build --configuration Release \
		--settings coverlet.runsettings --results-directory TestResults
	@# Copy coverage from GUID subdirectory to stable path for extract-coverage.sh
	@COVERAGE_FILE=$$(find TestResults -name 'coverage.cobertura.xml' -path '*/TestResults/*/coverage.cobertura.xml' | head -1); \
	if [ -n "$$COVERAGE_FILE" ]; then \
		cp "$$COVERAGE_FILE" TestResults/coverage.cobertura.xml; \
		echo "Coverage report: TestResults/coverage.cobertura.xml"; \
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
