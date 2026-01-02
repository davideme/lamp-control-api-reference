# PostgreSQL Implementation Guide

This guide provides step-by-step instructions for implementing PostgreSQL storage support across all language implementations.

## Completed Implementations

### ✅ C# (.NET 8.0)

**Status**: Fully implemented and tested (63/63 tests passing)

**Key Components**:
- `LampDbEntity` - Database entity record with init setters
- `LampControlDbContext` - EF Core DbContext with proper mappings
- `PostgresLampRepository` - Repository implementation using EF Core
- Integration tests using Testcontainers
- Environment-based configuration (no hardcoded connection strings)

**Configuration**:
```bash
# Using user secrets (recommended for development)
dotnet user-secrets set "ConnectionStrings:LampControl" "Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass"

# Or using environment variable
export ConnectionStrings__LampControl="Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass"
```

**Running with PostgreSQL**:
```bash
# Start PostgreSQL
docker run -d --name lampcontrol-postgres -e POSTGRES_DB=lampcontrol -e POSTGRES_USER=lampuser -e POSTGRES_PASSWORD=lamppass -p 5432:5432 postgres:16-alpine

# Apply schema
docker exec -i lampcontrol-postgres psql -U lampuser -d lampcontrol < ../../../database/sql/postgresql/schema.sql

# Configure and run
cd src/csharp
dotnet user-secrets set "ConnectionStrings:LampControl" "Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass"
cd LampControlApi && dotnet run
```

## Pending Implementations

### 🔄 Go (In Progress)

**ADR Reference**: `src/go/adr/005-postgresql-storage.md`

**Technology Stack**: pgx v5 + sqlc

**Implementation Steps**:

1. **Add dependencies** to `go.mod`:
```bash
go get github.com/jackc/pgx/v5
go get github.com/jackc/pgx/v5/pgxpool
```

2. **Install sqlc**:
```bash
go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest
```

3. **Create sqlc configuration** (`sqlc.yaml`):
```yaml
version: "2"
sql:
  - engine: "postgresql"
    queries: "internal/repository/queries"
    schema: "../../../database/sql/postgresql"
    gen:
      go:
        package: "repository"
        out: "internal/repository"
        sql_package: "pgx/v5"
        emit_json_tags: true
        emit_interface: true
        emit_prepared_queries: false
        emit_pointers_for_null_types: true
```

4. **Create SQL queries** in `internal/repository/queries/lamps.sql`:
```sql
-- name: CreateLamp :one
INSERT INTO lamps (id, is_on, created_at, updated_at)
VALUES ($1, $2, $3, $4)
RETURNING id, is_on, created_at, updated_at, deleted_at;

-- name: GetLampByID :one
SELECT id, is_on, created_at, updated_at, deleted_at
FROM lamps
WHERE id = $1 AND deleted_at IS NULL;

-- name: ListLamps :many
SELECT id, is_on, created_at, updated_at, deleted_at
FROM lamps
WHERE deleted_at IS NULL
ORDER BY created_at ASC;

-- name: UpdateLamp :one
UPDATE lamps
SET is_on = $2, updated_at = $3
WHERE id = $1 AND deleted_at IS NULL
RETURNING id, is_on, created_at, updated_at, deleted_at;

-- name: DeleteLamp :execrows
UPDATE lamps
SET deleted_at = $2
WHERE id = $1 AND deleted_at IS NULL;
```

5. **Generate code**:
```bash
sqlc generate
```

6. **Implement PostgresLampRepository** adapting the sqlc-generated code to the existing `LampRepository` interface

7. **Update main.go** to detect PostgreSQL configuration and use appropriate repository

8. **Create integration tests** using testcontainers-go

### 📝 Python (FastAPI)

**ADR Reference**: `src/python/docs/adr/007-postgresql-storage.md`

**Technology Stack**: SQLAlchemy 2.0 + asyncpg

**Implementation Steps**:

1. **Add dependencies** to `pyproject.toml`:
```toml
[tool.poetry.dependencies]
sqlalchemy = "^2.0.0"
asyncpg = "^0.29.0"
alembic = "^1.13.0"  # For migrations
```

2. **Create database models** in `src/app/infrastructure/database/models.py`

3. **Create SQLAlchemy repository** implementing the existing repository interface

4. **Update FastAPI app** to detect PostgreSQL configuration

5. **Create integration tests** using testcontainers

### 📝 TypeScript (Node.js + Express)

**ADR Reference**: `src/typescript/docs/adr/007-postgresql-storage.md`

**Technology Stack**: node-postgres (pg) + TypeORM or Prisma

**Implementation Steps**:

1. **Add dependencies**:
```bash
npm install pg @types/pg
# Choose one:
npm install typeorm reflect-metadata
# or
npm install @prisma/client && npm install -D prisma
```

2. **Create entity models**

3. **Implement PostgreSQL repository**

4. **Update Express app** configuration

5. **Create integration tests**

### 📝 Java (Spring Boot)

**ADR Reference**: `src/java/adr/007-postgresql-storage.md`

**Technology Stack**: Spring Data JPA + PostgreSQL JDBC Driver

**Implementation Steps**:

1. **Add dependencies** to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

2. **Create JPA entities**

3. **Create Spring Data JPA repository interface**

4. **Update application.properties**

5. **Create integration tests**

### 📝 Kotlin (Ktor)

**ADR Reference**: `src/kotlin/adr/006-postgresql-storage.md`

**Technology Stack**: Exposed + PostgreSQL JDBC Driver

**Implementation Steps**:

1. **Add dependencies** to `build.gradle.kts`

2. **Create Exposed table definitions**

3. **Implement repository using Exposed**

4. **Update Ktor application**

5. **Create integration tests**

## Testing Strategy

All implementations should include:

1. **Integration tests** using Testcontainers to start actual PostgreSQL instances
2. **Unit tests** for repository logic (if applicable)
3. **E2E tests** ensuring existing API tests still pass

## Common Patterns

### Environment Variable Configuration

All implementations should support these environment variables:
- `DATABASE_URL` or `POSTGRES_CONNECTION_STRING` - Full connection string
- `DB_HOST` - PostgreSQL host (default: localhost)
- `DB_PORT` - PostgreSQL port (default: 5432)
- `DB_NAME` - Database name (default: lampcontrol)
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password
- `DB_POOL_MIN_SIZE` - Minimum connection pool size
- `DB_POOL_MAX_SIZE` - Maximum connection pool size

### Schema Management

**Recommended approach**: Manual schema application using the existing `database/sql/postgresql/schema.sql`

```bash
psql -h localhost -U lampuser -d lampcontrol -f database/sql/postgresql/schema.sql
```

### Soft Deletes

All implementations must respect the soft delete pattern:
- Set `deleted_at` timestamp instead of physically deleting records
- Filter out soft-deleted records in queries (using query filters or WHERE clauses)

### Connection Pooling

Configure appropriate connection pool sizes based on expected load:
- Development: 5-10 connections
- Production: 20-50 connections (depends on workload)

## Validation Checklist

For each implementation:
- [ ] Dependencies added and documented
- [ ] Database entity/model created matching schema
- [ ] Repository implementation completed
- [ ] Connection pooling configured
- [ ] Environment-based configuration implemented
- [ ] Soft delete support implemented
- [ ] Integration tests created and passing
- [ ] Existing E2E tests still passing
- [ ] README updated with PostgreSQL setup instructions
- [ ] Health check endpoint includes database connectivity (optional)

## Resources

- PostgreSQL schema: `database/sql/postgresql/schema.sql`
- ADRs: `src/<language>/adr/` or `src/<language>/docs/adr/`
- Testcontainers documentation: https://testcontainers.com/
- PostgreSQL documentation: https://www.postgresql.org/docs/
