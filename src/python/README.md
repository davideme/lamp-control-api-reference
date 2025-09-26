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
poetry run fastapi run src/openapi_server/main.py --port 8080 --host 0.0.0.0
```

and open your browser at `http://localhost:8080/docs/` to see the docs.

## Health Check

The API includes a health check endpoint for monitoring:

- `GET /health` - Returns `{"status": "ok"}` with HTTP 200 when the service is healthy

## Running with Docker

To run the server on a Docker container, please execute the following from the root directory:

```bash
docker-compose up --build
```

### Docker Image Features

The Docker image uses:
- **Multi-stage build**: Build stage with Poetry dependency installation, production stage with Google's distroless Python image
- **Poetry dependency management**: Uses Poetry in build stage for reproducible dependency installation
- **Distroless runtime**: Production stage uses `gcr.io/distroless/python3-debian12:nonroot` for minimal attack surface and non-root execution
- **Port Configuration**: Supports PORT environment variable for Cloud Run compatibility via launcher script
- **FastAPI CLI**: Uses the modern `fastapi run` command for production deployment

#### Building Docker Image

```bash
docker build -t lamp-control-api-python .
```

#### Running Docker Container

```bash
# Run with default port (80)
docker run -p 8080:80 lamp-control-api-python

# Run with custom port via environment variable (Cloud Run style)
docker run -p 8080:8080 -e PORT=8080 lamp-control-api-python
```

## Cloud Run Deployment

This application is optimized for Google Cloud Run deployment:

### Environment Variables

- `PORT` - The port for the HTTP server. Cloud Run automatically sets this variable for the ingress container. Defaults to 80 if not set.

### Cloud Run Notes

- The following environment variables are automatically added to all running containers except `PORT`. The `PORT` variable is only added to the ingress container.
- The application will bind to `0.0.0.0:${PORT}` to accept traffic from Cloud Run's load balancer.
- The Docker image uses a multi-stage build for optimal size and security.

### Deployment Example

```bash
# Deploy to Cloud Run
gcloud run deploy lamp-control-api \
  --image gcr.io/YOUR_PROJECT/lamp-control-api-python \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

## Tests

To run the tests:

```bash
poetry run pytest tests
```
