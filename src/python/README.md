# Lamp Control API (Python Implementation)

Python implementation of the Lamp Control API, providing endpoints to control and monitor lamp status.

## Requirements

- Python 3.12.9 or higher
- Poetry (Python dependency management tool)

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

1. Install Poetry (if not already installed):
   ```bash
   curl -sSL https://install.python-poetry.org | python3 -
   ```

2. Install project dependencies:
   ```bash
   poetry install
   ```

3. Activate the virtual environment:
   ```bash
   poetry shell
   ```

4. Install pre-commit hooks:
   ```bash
   poetry run pre-commit install
   ```

## Running Tests

```bash
poetry run pytest
```

## Code Quality

This project uses several tools to ensure code quality:

- `black` for code formatting
- `ruff` for linting
- `mypy` for static type checking
- `pytest` for testing

Run all quality checks:
```bash
poetry run black .
poetry run ruff check .
poetry run mypy .
poetry run pytest
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

For more details about the dependency management decision, see [ADR 011: Python Dependency Management Tool Selection](../../docs/adr/011-python-dependency-management.md)
