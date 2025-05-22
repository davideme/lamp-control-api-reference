# Makefile for generating and validating Java Spring server stubs from OpenAPI spec

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
	@echo "Generating Spring server stub..."
	${DOCKER_RUN} generate \
		-i /local/${OPENAPI_SPEC} \
		-g ${GENERATOR_NAME} \
		-o /local/${OUTPUT_DIR} \
		--additional-properties=async=true,useSpringBoot3=true,useOptional=true,hideGenerationTimestamp=true,performBeanValidation=true

# Clean generated files
clean:
	@echo "Cleaning generated files..."
	rm -rf ${OUTPUT_DIR}/lamp_control_api

# Validate OpenAPI specification
validate:
	@echo "Validating OpenAPI specification..."
	${DOCKER_RUN} validate -i /local/${OPENAPI_SPEC}

# Help target
help:
	@echo "Available targets:"
	@echo "  generate  - Generate Spring server stub"
	@echo "  clean    - Remove generated files"
	@echo "  validate - Validate OpenAPI specification"
	@echo "  help     - Show this help message" 