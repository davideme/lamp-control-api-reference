# Lamp Control API Reference

[![Database Schema Tests](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-tests.yml/badge.svg)](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-tests.yml)

A comprehensive reference implementation of a simple lamp control API in multiple popular web programming languages, showcasing different API styles (REST, GraphQL, and gRPC) and database technologies.

## Project Overview

This project aims to provide a clear, concise reference implementation that demonstrates proper API design and implementation patterns across different programming languages and frameworks for web API development.

The API allows for basic CRUD operations on a simple lamp resource (with ID and on/off state), implemented consistently across multiple languages and API interface styles.

## Languages Included

- TypeScript
- Python
- Java
- PHP
- Ruby
- Go

## Key Features

- Consistent implementations across all languages
- Support for both SQL (MySQL/PostgreSQL) and MongoDB
- Three API interfaces (REST/OpenAPI 3.0+, GraphQL, gRPC)
- Comprehensive test coverage
- Standardized documentation

## Development Requirements

For the Node.js/TypeScript implementation, the following versions are required:

- **Node.js:** `>=20.x`
- **npm:** `>=10.x`

Refer to [ADR-002](docs/adr/002-nodejs-and-npm-versions.md) for details.

For the Python implementation requirements, see [Python README](src/python/README.md).

## Documentation

For full details on the project requirements and specifications, see the [Product Requirements Document](docs/PRD.md).

## Project Structure

```
lamp-control-api-reference/
├── docs/
│   └── PRD.md
├── src/
│   ├── typescript/
│   ├── python/
│   ├── java/
│   ├── php/
│   ├── ruby/
│   └── go/
└── README.md
```

Each language implementation will follow a standardized structure and include support for all required API interfaces and database technologies.

## Metrics for Comparison

- Lines of code
- Test coverage
- API interface comparison
