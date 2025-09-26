---
applyTo: "src/python/**/*"
---

This Python implementation of the lamp control API uses FastAPI and modern Python practices.

Key frameworks and tools:
- FastAPI for the web framework with automatic OpenAPI documentation
- Poetry for dependency management
- pytest for testing
- Uvicorn as the ASGI server
- Pydantic for data validation and serialization
- OpenAPI Generator for code generation
- Black for code formatting
- Ruff for linting and code analysis

When working on this codebase:
- Follow Python 3.12+ features and syntax
- Use type hints consistently throughout the codebase
- Follow the FastAPI patterns for routing, dependency injection, and response models
- Maintain the repository pattern established in the codebase
- Write comprehensive tests using pytest, following the existing test structure
- Use async/await for asynchronous operations where appropriate
- Follow PEP 8 style guidelines
- Use Poetry for managing dependencies (pyproject.toml)
- Maintain consistency with the lamp domain model and OpenAPI specification
- Handle errors gracefully with proper HTTP status codes
- Use Pydantic models for request/response validation
- **ALWAYS run `poetry run black .` and `poetry run ruff check .` before any commit to ensure code formatting and quality**
- Use the Makefile targets for development workflow (format, lint, test, build)