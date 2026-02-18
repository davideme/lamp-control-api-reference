# Mode Testing Scripts

This directory contains reusable scripts for testing the three operation modes (migrate, serve-only, serve) across all language implementations.

## Overview

The LAMP Control API supports three operation modes:

1. **migrate** - Run database migrations only and exit (production phase 1)
2. **serve-only** - Start server without migrations (production phase 2, default mode)
3. **serve** - Run migrations and start server (local development)

### Production Pattern

The production deployment pattern uses two phases:
1. Run `migrate` mode to apply database schema changes
2. Run `serve-only` mode to start application server(s)

This pattern allows:
- Zero-downtime deployments
- Horizontal scaling (multiple servers after single migration)
- Separation of concerns (schema changes vs. serving traffic)

### Local Development Pattern

For local development, use `serve` mode which combines both phases:
- Automatically runs migrations on startup
- Starts the server
- Convenient for rapid iteration

## Scripts

### `ci/test-modes.sh`

Core testing logic for all three operation modes. Language-agnostic script that takes language-specific commands as parameters.

**Usage:**
```bash
./test-modes.sh <language> <build_cmd> <migrate_cmd> <serve_only_cmd> <serve_cmd> <health_port_serve_only> <health_port_serve>
```

**Example (Go):**
```bash
./test-modes.sh \
  "go" \
  "go build -o bin/lamp-control-api ./cmd/lamp-control-api" \
  "./bin/lamp-control-api --mode=migrate" \
  "./bin/lamp-control-api --mode=serve-only --port=8081" \
  "./bin/lamp-control-api --mode=serve --port=8082" \
  8081 \
  8082
```

**What it tests:**
1. **Migrate mode**: Runs migrations, verifies tables created, exits cleanly
2. **Serve-only mode**: Starts server, verifies health endpoint, validates existing schema (from migrate), tests API
3. **Serve mode**: Creates new database, runs migrations + starts server, verifies everything works

### `ci/languages.sh`

Language-specific configuration file with commands and settings for all implementations.

**Supported Languages:**
- Go
- Python
- Java
- C#
- Kotlin
- TypeScript

**Configuration includes:**
- Build commands
- Migrate/serve-only/serve mode commands
- Health endpoint ports
- Working directories

**Functions:**
- `get_language_config <language> <config_type>` - Get specific configuration value
- `list_languages` - List all supported languages

**Example:**
```bash
source ci/languages.sh
BUILD_CMD=$(get_language_config "go" "build")
# Returns: "go build -o bin/lamp-control-api ./cmd/lamp-control-api"
```

### `ci/setup-test-databases.sh`

Database setup and cleanup utilities for testing.

**Functions:**
- `create_database <dbname>` - Create PostgreSQL database
- `drop_database <dbname>` - Drop PostgreSQL database
- `clean_databases` - Drop all test databases

**Environment Variables:**
- `POSTGRES_HOST` (default: localhost)
- `POSTGRES_PORT` (default: 5432)
- `POSTGRES_USER` (default: test)
- `POSTGRES_PASSWORD` (default: test)

**Example:**
```bash
source ci/setup-test-databases.sh
create_database lampcontrol_prod
```

### `ci/verify-database.sh`

Database state verification utilities.

**Usage:**
```bash
./verify-database.sh <dbname> <state> [table]
```

**States:**
- `exists` - Verify table exists
- `not-exists` - Verify table does not exist

**Example:**
```bash
./verify-database.sh lampcontrol_prod exists lamp
# Verifies 'lamp' table exists in lampcontrol_prod database
```

### `test-modes-local.sh`

Local test runner with PostgreSQL container management.

**Usage:**
```bash
./test-modes-local.sh <language|all> [options]
```

**Options:**
- `--start-postgres` - Start PostgreSQL container before testing
- `--stop-postgres` - Stop PostgreSQL container after testing
- `--keep-postgres` - Keep PostgreSQL container running after tests

**Examples:**
```bash
# Test Go implementation with automatic PostgreSQL setup
./test-modes-local.sh go --start-postgres

# Test all languages with automatic PostgreSQL setup
./test-modes-local.sh all --start-postgres

# Test Python with existing PostgreSQL
./test-modes-local.sh python

# Keep PostgreSQL running for multiple test runs
./test-modes-local.sh go --start-postgres --keep-postgres
./test-modes-local.sh python  # Reuses existing container
```

## Local Testing

### Prerequisites

1. Docker (for PostgreSQL container)
2. Language-specific tools (Go, Python, Java, etc.)
3. psql command-line tool

### Quick Start

Test a single language:
```bash
cd /path/to/lamp-control-api-reference
./scripts/test-modes-local.sh go --start-postgres
```

Test all languages:
```bash
./scripts/test-modes-local.sh all --start-postgres
```

### Manual PostgreSQL Setup

If you prefer to manage PostgreSQL yourself:

```bash
# Start PostgreSQL container
docker run -d \
  --name mode-test-postgres \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=lampcontrol \
  -p 5432:5432 \
  postgres:16-alpine

# Run tests
./scripts/test-modes-local.sh go

# Stop container when done
docker stop mode-test-postgres
docker rm mode-test-postgres
```

## CI Integration

These scripts are used by GitHub Actions workflows for all language implementations.

**Workflow Example:**
```yaml
mode-testing:
  name: Mode Testing
  runs-on: ubuntu-latest
  needs: [test]

  services:
    postgres:
      image: postgres:16-alpine
      env:
        POSTGRES_USER: test
        POSTGRES_PASSWORD: test
        POSTGRES_DB: lampcontrol
      ports:
        - 5432:5432
      options: >-
        --health-cmd pg_isready
        --health-interval 10s
        --health-timeout 5s
        --health-retries 5

  defaults:
    run:
      working-directory: src/go

  steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up Go
      uses: actions/setup-go@v4
      with:
        go-version: '1.24'

    - name: Test all operation modes
      run: |
        ../../scripts/ci/test-modes.sh \
          "go" \
          "go build -o bin/lamp-control-api ./cmd/lamp-control-api" \
          "./bin/lamp-control-api --mode=migrate" \
          "./bin/lamp-control-api --mode=serve-only --port=8081" \
          "./bin/lamp-control-api --mode=serve --port=8082" \
          8081 \
          8082
```

## Architecture

### Test Flow

1. **Build Phase** (if build_cmd provided)
   - Runs language-specific build command
   - Prepares application for testing

2. **Migrate Mode Test**
   - Creates clean database (lampcontrol_prod)
   - Runs migrate mode
   - Verifies tables created
   - Verifies process exits cleanly
   - Leaves database intact for serve-only test

3. **Serve-Only Mode Test** (production pattern)
   - **Reuses database from migrate test** (simulates production)
   - Starts server without migrations
   - Tests health endpoint
   - Verifies schema exists (from migrate)
   - Tests API endpoint
   - Cleans up server process

4. **Serve Mode Test** (local development pattern)
   - Creates new clean database (lampcontrol_serve)
   - Starts server with migrations
   - Verifies migrations ran (tables created)
   - Tests health endpoint
   - Tests API endpoint
   - Cleans up server process

### Database Isolation

Each mode test uses separate databases:
- `lampcontrol_prod` - Used by migrate and serve-only tests (production pattern)
- `lampcontrol_serve` - Used by serve test (independent local dev pattern)

This isolation ensures:
- Tests don't interfere with each other
- Production pattern is accurately simulated
- Failures are easy to debug

### Port Allocation

Different ports prevent conflicts when testing multiple modes:
- Migrate mode: No server (exits immediately)
- Serve-only mode: Port 8081 (or language-specific)
- Serve mode: Port 8082 (or language-specific)

## Troubleshooting

### PostgreSQL Connection Issues

If tests fail with connection errors:

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs mode-test-postgres

# Verify port is available
lsof -i :5432
```

### Test Failures

Check the colored output for specific failures:
- ✓ (green) - Test passed
- ✗ (red) - Test failed
- ⚠ (yellow) - Warning

Common issues:
- **Migrate mode fails**: Check database connection, verify schema SQL files
- **Serve-only mode fails**: Ensure migrate test passed (schema must exist)
- **Serve mode fails**: Check for port conflicts, verify migrations work

### Debugging

Enable verbose output:
```bash
# Run test-modes.sh with bash debug mode
bash -x scripts/ci/test-modes.sh go "..." "..." "..." "..." 8081 8082
```

Check process status:
```bash
# List all running processes for your language
ps aux | grep lamp-control-api
ps aux | grep python
ps aux | grep java
```

## Contributing

When adding a new language implementation:

1. Add configuration to `ci/languages.sh`:
   ```bash
   LANGUAGE_BUILD["newlang"]="build command"
   LANGUAGE_MIGRATE["newlang"]="migrate command"
   LANGUAGE_SERVE_ONLY["newlang"]="serve-only command"
   LANGUAGE_SERVE["newlang"]="serve command"
   LANGUAGE_HEALTH_PORT_SERVE_ONLY["newlang"]="8081"
   LANGUAGE_HEALTH_PORT_SERVE["newlang"]="8082"
   LANGUAGE_WORKDIR["newlang"]="src/newlang"
   ```

2. Add workflow file `.github/workflows/newlang-ci.yml` with mode-testing job

3. Test locally:
   ```bash
   ./scripts/test-modes-local.sh newlang --start-postgres
   ```

## References

- [GitHub Actions Service Containers](https://docs.github.com/en/actions/using-containerized-services/about-service-containers)
- [PostgreSQL Docker Images](https://hub.docker.com/_/postgres)
- [Bash Best Practices](https://google.github.io/styleguide/shellguide.html)
