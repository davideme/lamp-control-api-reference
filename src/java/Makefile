# Makefile for OpenAPI code generation in Java Spring project
# Maven handles all build, test, format, and lint tasks - use 'mvn' commands directly

# Variables
OPENAPI_GENERATOR_VERSION := v7.13.0
OPENAPI_SPEC := docs/api/openapi.yaml
OUTPUT_DIR := src/java
GENERATOR_NAME := spring

# Docker command with common options
DOCKER_RUN := docker run --rm \
	-v ${PWD}/../..:/local \
	openapitools/openapi-generator-cli:${OPENAPI_GENERATOR_VERSION}

.PHONY: generate clean validate help

# Generate Spring server stub
generate:
	@echo "Generating Spring server stub from OpenAPI specification..."
	${DOCKER_RUN} generate \
		-i /local/${OPENAPI_SPEC} \
		-g ${GENERATOR_NAME} \
		-o /local/${OUTPUT_DIR} \
		--additional-properties=async=true,useSpringBoot3=true,useOptional=true,hideGenerationTimestamp=true,performBeanValidation=true,interfaceOnly=true

# Clean generated files
clean:
	@echo "Cleaning generated OpenAPI files..."
	rm -rf ${OUTPUT_DIR}/lamp_control_api

# Validate OpenAPI specification
validate:
	@echo "Validating OpenAPI specification..."
	${DOCKER_RUN} validate -i /local/${OPENAPI_SPEC}

# Help target
help:
	@echo "OpenAPI Code Generation for Java Spring Boot Project"
	@echo ""
	@echo "Available targets:"
	@echo "  generate  - Generate Spring server stub from OpenAPI spec"
	@echo "  clean     - Remove generated OpenAPI files"
	@echo "  validate  - Validate OpenAPI specification"
	@echo "  help      - Show this help message"
	@echo ""
	@echo "For build tasks, use Maven directly:"
	@echo "  mvn clean compile          - Clean and compile"
	@echo "  mvn test                   - Run tests"
	@echo "  mvn spotless:apply         - Format code"
	@echo "  mvn spotless:check         - Check formatting"
	@echo "  mvn spotbugs:check         - Run static analysis"
	@echo "  mvn clean install          - Full build with tests" 