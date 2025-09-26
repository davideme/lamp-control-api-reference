# Variables
OPENAPI_GENERATOR_VERSION := v7.13.0
OPENAPI_SPEC := docs/api/openapi.yaml
OUTPUT_DIR := src/python
GENERATOR_NAME := python-fastapi

# Docker command with common options
DOCKER_RUN := docker run --rm \
	-v ${PWD}/../..:/local \
	openapitools/openapi-generator-cli:${OPENAPI_GENERATOR_VERSION}

.PHONY: generate clean validate format lint format-check test install help

# Generate FastAPI server stub
generate:
	@echo "Generating FastAPI server stub..."
	${DOCKER_RUN} generate \
		-i /local/${OPENAPI_SPEC} \
		-g ${GENERATOR_NAME} \
		-o /local/${OUTPUT_DIR}

# Clean generated files
clean:
	@echo "Cleaning generated files..."
	rm -rf ${OUTPUT_DIR}/lamp_control_api

# Validate OpenAPI specification
validate:
	@echo "Validating OpenAPI specification..."
	${DOCKER_RUN} validate -i /local/${OPENAPI_SPEC}

# Install dependencies
install:
	@echo "Installing dependencies with Poetry..."
	poetry install

# Format code with black
format:
	@echo "Formatting code with black..."
	poetry run black .

# Check code formatting
format-check:
	@echo "Checking code formatting with black..."
	poetry run black --check .

# Lint code with ruff
lint:
	@echo "Linting code with ruff..."
	poetry run ruff check .

# Run tests
test:
	@echo "Running tests with pytest..."
	poetry run pytest

# Development workflow
dev: format lint test

# CI/CD workflow
ci: format-check lint test

# Help target
help:
	@echo "Available targets:"
	@echo "  generate     - Generate FastAPI server stub"
	@echo "  clean        - Remove generated files"  
	@echo "  validate     - Validate OpenAPI specification"
	@echo "  install      - Install dependencies with Poetry"
	@echo "  format       - Format code with black"
	@echo "  format-check - Check code formatting with black"
	@echo "  lint         - Lint code with ruff"
	@echo "  test         - Run tests with pytest"
	@echo "  dev          - Run development workflow (format, lint, test)"
	@echo "  ci           - Run CI workflow (format-check, lint, test)"
	@echo "  help         - Show this help message" 