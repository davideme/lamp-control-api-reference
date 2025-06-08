# Makefile for OpenAPI code generation in Java Spring project
# Maven handles all build, test, format, and lint tasks - use 'mvn' commands directly

# Variables
OPENAPI_GENERATOR_VERSION := v7.13.0
OPENAPI_SPEC := docs/api/openapi.yaml
OUTPUT_DIR := src/php
GENERATOR_NAME := php-slim4

# Docker command with common options
DOCKER_RUN := docker run --rm \
	-v ${PWD}/../..:/local \
	openapitools/openapi-generator-cli:${OPENAPI_GENERATOR_VERSION}

.PHONY: generate clean validate help

# Generate PHP Slim 4 server stub
generate:
	@echo "Generating Spring server stub from OpenAPI specification..."
	${DOCKER_RUN} generate \
		-i /local/${OPENAPI_SPEC} \
		-g ${GENERATOR_NAME} \
		-o /local/${OUTPUT_DIR}

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
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m\033[0m\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)
