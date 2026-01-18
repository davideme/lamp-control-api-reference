# Operation Modes Across All Implementations

All implementations of the Lamp Control API now support three distinct operation modes for better control over database migrations and server startup. This is particularly useful for production deployments, CI/CD pipelines, and container orchestration.

## The Three Modes

### 1. **`serve`** (Default Mode)
Runs database migrations first, then starts the HTTP server.

**Use case:** Development environments, simple deployments where you want automatic migrations.

### 2. **`migrate`** (Migration-Only Mode)
Runs database migrations and exits without starting the server.

**Use case:** Production deployments using init containers, CI/CD pipelines, manual migration execution.

### 3. **`serve-only`** (Server-Only Mode)
Starts the HTTP server without running migrations.

**Use case:** Production environments where migrations are run separately (e.g., by a dedicated migration pod/container).

---

## Usage by Implementation

### Go

```bash
# Default: Run migrations and start server
./lamp-control-api --mode=serve

# Run migrations only
./lamp-control-api --mode=migrate

# Start server without migrations
./lamp-control-api --mode=serve-only
```

**Build and run:**
```bash
cd src/go
go build -o lamp-control-api ./cmd/lamp-control-api
./lamp-control-api --mode=serve
```

---

### Kotlin (Ktor)

```bash
# Default: Run migrations and start server
java -jar build/libs/lamp-control-api.jar --mode=serve

# Run migrations only
java -jar build/libs/lamp-control-api.jar --mode=migrate

# Start server without migrations
java -jar build/libs/lamp-control-api.jar --mode=serve-only
```

**Build and run:**
```bash
cd src/kotlin
./gradlew build
java -jar build/libs/*.jar --mode=serve
```

---

### Java (Spring Boot)

```bash
# Default: Run migrations and start server
java -jar target/lamp-control-api.jar --mode=serve

# Run migrations only
java -jar target/lamp-control-api.jar --mode=migrate

# Start server without migrations
java -jar target/lamp-control-api.jar --mode=serve-only
```

**Build and run:**
```bash
cd src/java
mvn clean package
java -jar target/*.jar --mode=serve
```

---

### Python (FastAPI)

```bash
# Default: Run migrations and start server
python -m src.openapi_server.cli --mode=serve

# Run migrations only
python -m src.openapi_server.cli --mode=migrate

# Start server without migrations
python -m src.openapi_server.cli --mode=serve-only
```

**With Poetry:**
```bash
cd src/python
poetry install
poetry run python -m src.openapi_server.cli --mode=serve
```

---

### TypeScript (Fastify)

```bash
# Default: Run migrations and start server
node dist/cli.js --mode=serve

# Run migrations only
node dist/cli.js --mode=migrate

# Start server without migrations
node dist/cli.js --mode=serve-only
```

**Build and run:**
```bash
cd src/typescript
npm install
npm run build
node dist/cli.js --mode=serve
```

---

### C# (.NET)

```bash
# Default: Run migrations and start server
dotnet run --project LampControlApi -- --mode=serve

# Run migrations only
dotnet run --project LampControlApi -- --mode=migrate

# Start server without migrations
dotnet run --project LampControlApi -- --mode=serve-only
```

**Build and run:**
```bash
cd src/csharp
dotnet build
dotnet run --project LampControlApi -- --mode=serve
```

---

## Production Deployment Patterns

### Pattern 1: Init Container (Kubernetes)

Run migrations in an init container, then start the application:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lamp-control-api
spec:
  template:
    spec:
      initContainers:
        - name: migrations
          image: lamp-control-api:latest
          args: ["--mode=migrate"]
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
      containers:
        - name: app
          image: lamp-control-api:latest
          args: ["--mode=serve-only"]
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
```

### Pattern 2: Separate Migration Job

Run migrations as a one-off job before deployment:

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: lamp-control-migrate
spec:
  template:
    spec:
      containers:
        - name: migrate
          image: lamp-control-api:latest
          args: ["--mode=migrate"]
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
      restartPolicy: Never
```

Then deploy the application with `--mode=serve-only`.

### Pattern 3: CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml
steps:
  - name: Run Database Migrations
    run: |
      docker run --rm \
        -e DATABASE_URL=${{ secrets.DATABASE_URL }} \
        lamp-control-api:${{ github.sha }} \
        --mode=migrate

  - name: Deploy Application
    run: |
      # Deploy with serve-only mode
      kubectl set image deployment/lamp-control-api \
        app=lamp-control-api:${{ github.sha }}
      kubectl set env deployment/lamp-control-api MODE=serve-only
```

---

## Benefits of the Three-Mode Approach

1. **Separation of Concerns**: Migrations and application startup are decoupled
2. **Zero-Downtime Deployments**: Run migrations before deploying new application versions
3. **Better Control**: Explicit control over when and how migrations run
4. **Debugging**: Run migrations separately to troubleshoot issues
5. **Security**: Migrations can run with elevated privileges while the app runs with restricted access
6. **Rollback Safety**: Verify migrations succeed before starting the application

---

## Migration Tool by Implementation

| Language   | Migration Tool      | Location                           |
|------------|---------------------|------------------------------------|
| Go         | golang-migrate      | `src/go/api/migrations/`          |
| Kotlin     | Flyway              | `src/kotlin/src/main/resources/db/migration/` |
| Java       | Flyway              | `src/java/src/main/resources/db/migration/` |
| Python     | Alembic             | `src/python/alembic/versions/`    |
| TypeScript | Prisma Migrate      | `src/typescript/prisma/migrations/` |
| C#         | EF Core Migrations  | Managed by Entity Framework       |

---

## Troubleshooting

### Migrations fail in `serve` mode
- Check database connectivity
- Verify DATABASE_URL or connection configuration
- Run with `--mode=migrate` to see detailed migration output
- Check migration file syntax

### Migrations don't run in `serve` mode
- Ensure PostgreSQL is configured (DATABASE_URL is set)
- Check that migrations are embedded/included in the build
- Verify migration tool dependencies are installed

### Application starts but database schema is outdated
- You may have used `--mode=serve-only` when you should have used `--mode=serve`
- Run `--mode=migrate` manually to update the schema
- Check that the correct database is being targeted

---

## Environment Variables

All implementations respect these environment variables for PostgreSQL configuration:

- `DATABASE_URL`: Full PostgreSQL connection string (preferred)
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name
- `DB_USER`: Database user
- `DB_PASSWORD`: Database password
- `DB_POOL_MIN_SIZE`: Minimum pool size
- `DB_POOL_MAX_SIZE`: Maximum pool size

If no PostgreSQL configuration is found, implementations fall back to in-memory storage.
