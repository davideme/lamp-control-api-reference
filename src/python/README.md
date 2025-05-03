# Lamp Control API

A FastAPI-based REST API for controlling lamps.

## Requirements.

Python >= 3.12
Poetry >= 1.0.0

## Installation & Usage

To run the server, please execute the following from the root directory:

```bash
# Install dependencies
poetry install

# Run the server
poetry run run-server
```

and open your browser at `http://localhost:8080/docs/` to see the docs.

## Running with Docker

To run the server on a Docker container, please execute the following from the root directory:

```bash
docker-compose up --build
```

## Tests

To run the tests:

```bash
poetry run pytest tests
```
