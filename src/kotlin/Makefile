# Makefile for OpenAPI code generation in Kotlin Ktor project
# Gradle handles all build, test, format, and lint tasks - use './gradlew' commands directly

# Variables
OPENAPI_GENERATOR_VERSION := v7.13.0
OPENAPI_SPEC := ../../docs/api/openapi.yaml
OUTPUT_DIR := generated
GENERATOR_NAME := kotlin-server

# Docker command with common options
DOCKER_RUN := docker run --rm \
	-v ${PWD}/../..:/local \
	openapitools/openapi-generator-cli:${OPENAPI_GENERATOR_VERSION}

.PHONY: generate clean validate help

# Generate Kotlin Ktor server stub
generate:
	@echo "Generating Ktor server stub from OpenAPI specification..."
	${DOCKER_RUN} generate \
		-i /local/docs/api/openapi.yaml \
		-g ${GENERATOR_NAME} \
		-o /local/src/kotlin/${OUTPUT_DIR} \
		--additional-properties=\
library=ktor,\
packageName=com.lampcontrol.api,\
serializationLibrary=kotlinx_serialization,\
enumPropertyNaming=UPPERCASE,\
dateLibrary=kotlinx-datetime

# Clean generated files
clean:
	@echo "Cleaning generated OpenAPI files..."
	rm -rf ${OUTPUT_DIR}

# Validate OpenAPI specification
validate:
	@echo "Validating OpenAPI specification..."
	${DOCKER_RUN} validate -i /local/${OPENAPI_SPEC}

# Help target
help:
	@echo "OpenAPI Code Generation for Kotlin Ktor Project"
	@echo ""
	@echo "Available targets:"
	@echo "  generate  - Generate Ktor server stub from OpenAPI spec"
	@echo "  clean     - Remove generated OpenAPI files"
	@echo "  validate  - Validate OpenAPI specification"
	@echo "  help      - Show this help message"
	@echo ""
	@echo "For build tasks, use Gradle directly:"
	@echo "  ./gradlew build            - Build the project"
	@echo "  ./gradlew test             - Run tests"
	@echo "  ./gradlew run              - Run the application"
	@echo "  ./gradlew ktlintCheck      - Check code style"
	@echo "  ./gradlew ktlintFormat     - Format code"
