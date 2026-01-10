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

# Run the server (with in-memory storage)
poetry run fastapi run src/openapi_server/main.py --port 8080 --host 0.0.0.0
```

and open your browser at `http://localhost:8080/docs/` to see the docs.

### Storage Backends

The API supports two storage backends:

1. **In-Memory Storage** (Default): Simple dictionary-based storage for development and testing
2. **PostgreSQL**: Persistent storage for production deployments

See the [PostgreSQL Setup](#postgresql-setup) section below for instructions on enabling PostgreSQL.

## Health Check

The API includes a health check endpoint for monitoring:

- `GET /health` - Returns `{"status": "ok", "storage": "memory" | "postgres"}` with HTTP 200 when the service is healthy

## PostgreSQL Setup

### Option 1: Using Docker Compose (Recommended for Local Development)

The easiest way to set up PostgreSQL locally is using Docker Compose:

```bash
# Start PostgreSQL and the API
cd ../../docker  # Navigate to the docker directory at repository root
docker-compose up

# The API will be available at http://localhost:8080
# PostgreSQL will be available at localhost:5432
```

### Option 2: Manual PostgreSQL Setup

#### 1. Install and Start PostgreSQL

```bash
# On macOS with Homebrew
brew install postgresql@16
brew services start postgresql@16

# On Ubuntu/Debian
sudo apt-get install postgresql-16
sudo systemctl start postgresql
```

#### 2. Create Database and User

```bash
# Connect to PostgreSQL
psql postgres

# Create user and database
CREATE USER lamp_user WITH PASSWORD 'lamp_password';
CREATE DATABASE lamp_control OWNER lamp_user;
\q
```

#### 3. Apply Database Schema

```bash
# From the repository root
psql -U lamp_user -d lamp_control -f database/sql/postgresql/schema.sql
```

#### 4. Configure Environment Variables

Create a `.env` file in the `src/python` directory:

```bash
cp .env.example .env
```

Edit `.env` and uncomment the `DATABASE_URL` line:

```env
# Uncomment this line to enable PostgreSQL
DATABASE_URL=postgresql+asyncpg://lamp_user:lamp_password@localhost:5432/lamp_control

# Optional: Connection pool settings
DB_POOL_MIN_SIZE=5
DB_POOL_MAX_SIZE=20
```

**Note:** Simply setting `DATABASE_URL` enables PostgreSQL storage. If `DATABASE_URL` is not set (or commented out), the API will use in-memory storage.

#### 5. Run Database Migrations (Optional)

If you prefer using Alembic migrations instead of applying schema.sql directly:

```bash
# Run migrations
poetry run alembic upgrade head

# To create a new migration after model changes
poetry run alembic revision --autogenerate -m "Description of changes"
```

#### 6. Start the Server with PostgreSQL

```bash
poetry run fastapi run src/openapi_server/main.py --port 8080 --host 0.0.0.0
```

### Environment Variables Reference

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | Full PostgreSQL connection URL. If set, enables PostgreSQL storage; if not set, uses in-memory storage. | `None` (in-memory) |
| `DB_HOST` | PostgreSQL host (fallback if DATABASE_URL not set) | `localhost` |
| `DB_PORT` | PostgreSQL port (fallback if DATABASE_URL not set) | `5432` |
| `DB_NAME` | Database name (fallback if DATABASE_URL not set) | `lamp_control` |
| `DB_USER` | Database user (fallback if DATABASE_URL not set) | `lamp_user` |
| `DB_PASSWORD` | Database password (fallback if DATABASE_URL not set) | `lamp_password` |
| `DB_POOL_MIN_SIZE` | Minimum connection pool size | `5` |
| `DB_POOL_MAX_SIZE` | Maximum connection pool size | `20` |

### Verifying PostgreSQL Connection

After starting the server with PostgreSQL enabled, check the health endpoint:

```bash
curl http://localhost:8080/health
# Should return: {"status": "ok", "storage": "postgres"}
```

### Database Features

The PostgreSQL implementation includes:

- **Soft Deletes**: Deleted lamps are marked with a `deleted_at` timestamp rather than being removed
- **Auto-Timestamps**: The `updated_at` field is automatically updated by a database trigger
- **UUID Primary Keys**: Uses PostgreSQL's UUID type for lamp IDs
- **Connection Pooling**: Configurable connection pool for optimal performance
- **Async Support**: Fully async using SQLAlchemy 2.0 and asyncpg

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
- **Worker Configuration**: Supports WORKERS environment variable to control the number of worker processes via launcher script
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

# Run with custom port and workers
docker run -p 8080:8080 -e PORT=8080 -e WORKERS=5 lamp-control-api-python
```

## Cloud Run Deployment

This application is optimized for Google Cloud Run deployment:

### Environment Variables

- `PORT` - The port for the HTTP server. Cloud Run automatically sets this variable for the ingress container. Defaults to 80 if not set.
- `WORKERS` - The number of worker processes to run. Defaults to 3 if not set.

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

### Running Unit Tests

To run the in-memory repository unit tests:

```bash
poetry run pytest src/openapi_server/test/test_lamp_repository.py -v
```

### Running PostgreSQL Integration Tests

The integration tests use Testcontainers to spin up a real PostgreSQL database:

```bash
# Install test dependencies (including testcontainers)
poetry install --with dev

# Run PostgreSQL integration tests
# Note: Requires Docker to be running
poetry run pytest src/openapi_server/test/test_postgres_lamp_repository.py -v
```

### Running All Tests

```bash
poetry run pytest -v
```

### Test Coverage

```bash
poetry run pytest --cov=src --cov-report=term-missing
```

## Troubleshooting

### PostgreSQL Connection Issues

If you encounter connection errors:

1. **Check PostgreSQL is running**:
   ```bash
   psql -U lamp_user -d lamp_control -c "SELECT 1;"
   ```

2. **Verify environment variables**:
   ```bash
   poetry run python -c "from src.openapi_server.infrastructure.config import DatabaseSettings; s=DatabaseSettings(); print(s.get_connection_string())"
   ```

3. **Check database schema**:
   ```bash
   psql -U lamp_user -d lamp_control -c "\dt"
   # Should show the 'lamps' table
   ```

### Testcontainers Issues

If integration tests fail with Docker errors:

1. **Ensure Docker is running**:
   ```bash
   docker ps
   ```

2. **Check Docker permissions** (Linux):
   ```bash
   sudo usermod -aG docker $USER
   # Log out and back in
   ```

3. **Increase Docker resources** (macOS/Windows):
   - Open Docker Desktop
   - Go to Settings → Resources
   - Increase memory to at least 4GB
