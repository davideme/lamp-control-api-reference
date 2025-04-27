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

## Running the API Server

There are a few different ways to run the FastAPI application:

### Using Python directly

```bash
cd src/python
python -m uvicorn main:app --reload
```

### Using Poetry

```bash
cd src/python
poetry run python -m uvicorn main:app --reload
```

### Using the main.py script

```bash
cd src/python
chmod +x main.py  # Make executable if not already
./main.py
```

The API server will start and be available at http://localhost:8000 by default.

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

### Testing Endpoints

You can use the Swagger UI to test the API endpoints:

1. Create a lamp: `POST /lamps`
2. List all lamps: `GET /lamps`
3. Get a specific lamp: `GET /lamps/{lamp_id}`
4. Update a lamp status: `PUT /lamps/{lamp_id}`
5. Delete a lamp: `DELETE /lamps/{lamp_id}`

Example curl commands:

```bash
# Create a new lamp
curl -X POST "http://localhost:8000/lamps" \
  -H "Content-Type: application/json" \
  -d '{"status": true}'

# List all lamps
curl -X GET "http://localhost:8000/lamps"

# Get a specific lamp (replace {lamp_id} with an actual UUID)
curl -X GET "http://localhost:8000/lamps/{lamp_id}"

# Update a lamp's status (replace {lamp_id} with an actual UUID)
curl -X PUT "http://localhost:8000/lamps/{lamp_id}" \
  -H "Content-Type: application/json" \
  -d '{"status": false}'

# Delete a lamp (replace {lamp_id} with an actual UUID)
curl -X DELETE "http://localhost:8000/lamps/{lamp_id}"
```

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Run tests and quality checks
4. Submit a pull request

For more details about the API framework selection, see [ADR 012: Python HTTP Frameworks Selection](../../docs/adr/012-python-http-frameworks.md)

For more details about the dependency management decision, see [ADR 011: Python Dependency Management Tool Selection](../../docs/adr/011-python-dependency-management.md)
