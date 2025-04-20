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
