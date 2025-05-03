# Variables
OPENAPI_GENERATOR_VERSION := v7.3.0
OPENAPI_SPEC := docs/api/openapi.yaml
OUTPUT_DIR := src/python
GENERATOR_NAME := python-fastapi

# Docker command with common options
DOCKER_RUN := docker run --rm \
	-v ${PWD}/../..:/local \
	openapitools/openapi-generator-cli:${OPENAPI_GENERATOR_VERSION}

.PHONY: generate clean validate

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

# Help target
help:
	@echo "Available targets:"
	@echo "  generate  - Generate FastAPI server stub"
	@echo "  clean    - Remove generated files"
	@echo "  validate - Validate OpenAPI specification"
	@echo "  help     - Show this help message" 