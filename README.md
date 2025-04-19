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

# Lamp Control API (Python Implementation)

Python implementation of the Lamp Control API, providing endpoints to control and monitor lamp status.

## Requirements

- Python 3.12.9 or higher
- pip (Python package installer)
- virtualenv or venv (recommended)

## Project Structure

```
lamp_control/
├── api/        # API endpoints and routers
├── core/       # Core functionality and config
├── db/         # Database models and migrations
├── models/     # Domain models
├── schemas/    # Pydantic schemas
├── services/   # Business logic
└── utils/      # Utility functions
```

## Development Setup

1. Create and activate a virtual environment:
   ```bash
   python3.12 -m venv venv
   source venv/bin/activate  # On Windows: .\venv\Scripts\activate
   ```

2. Install development dependencies:
   ```bash
   pip install -r requirements-dev.txt
   ```

3. Install pre-commit hooks:
   ```bash
   pre-commit install
   ```

## Running Tests

```bash
pytest
```

## Code Quality

This project uses several tools to ensure code quality:

- `black` for code formatting
- `ruff` for linting
- `mypy` for static type checking
- `pytest` for testing

Run all quality checks:
```bash
black .
ruff check .
mypy .
pytest
```

## API Documentation

Once running, API documentation is available at:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Run tests and quality checks
4. Submit a pull request

## License

[Add appropriate license information]
