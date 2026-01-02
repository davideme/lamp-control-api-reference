# ADR 005: PostgreSQL Storage with pgx v5

## Status

Accepted

## Context

The Go implementation of the Lamp Control API currently uses an in-memory repository (`InMemoryLampRepository`) for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with proper concurrency handling, connection pooling, and high-performance data access.

### Current State

- **Framework**: Chi router v5.2.1 with oapi-codegen
- **Architecture**: `LampRepository` interface with `InMemoryLampRepository` implementation
- **Storage**: In-memory map with `sync.RWMutex` for concurrency
- **Dependencies**: No database libraries currently integrated

### Requirements

1. **High Performance**: Minimal overhead, efficient connection pooling
2. **Type Safety**: Strong PostgreSQL type support (UUID, timestamps, JSONB)
3. **Context Support**: Cancellation and timeout handling via `context.Context`
4. **Concurrency**: Safe concurrent access with connection pooling
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with real PostgreSQL instances

### Technology Landscape (2025-2026)

**pgx v5**
- Pure Go PostgreSQL driver (no CGO)
- 3-5x faster than database/sql for PostgreSQL
- Native PostgreSQL protocol implementation
- 11.5k+ GitHub stars, actively maintained
- Used by Uber, GitHub, Discord, Cloudflare
- Advanced features: LISTEN/NOTIFY, COPY protocol, query batching

**sqlx**
- Extension of database/sql with struct scanning
- Database-agnostic (multi-database support)
- Named queries, convenience methods
- Slower than pgx for PostgreSQL-specific operations

**GORM**
- Full-featured ORM with ActiveRecord pattern
- Migrations, associations, hooks, soft deletes
- 2-3x slower than pgx
- Overkill for simple REST APIs

**sqlc**
- Generates type-safe Go code from SQL queries
- Compile-time SQL validation
- Works with pgx, database/sql, or sqlx
- Zero runtime overhead
- Catches SQL errors at compile time
- 13k+ GitHub stars

## Decision

We will implement **pgx v5 with pgxpool + sqlc** as the PostgreSQL data access layer for the Go Lamp Control API implementation.

**Rationale for Combined Approach:**
- pgx v5 provides maximum performance and PostgreSQL-specific features
- sqlc adds compile-time type safety and SQL validation
- Together they provide the best of both worlds: performance + safety

### Architecture

```
Handlers → LampRepository → sqlc Generated Code → pgxpool.Pool → PostgreSQL
                                    ↓
                              SQL Queries (*.sql) → Compile-Time Validated
```

### Core Components

#### 1. **sqlc Configuration**

```yaml
# sqlc.yaml
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

#### 2. **SQL Queries with sqlc Annotations**

```sql
-- internal/repository/queries/lamps.sql

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
ORDER BY created_at ASC
LIMIT $1 OFFSET $2;

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

#### 3. **Connection Pool Configuration**

```go
package repository

import (
    "context"
    "fmt"
    "time"
    
    "github.com/jackc/pgx/v5/pgxpool"
)

type Config struct {
    Host         string
    Port         int
    Database     string
    User         string
    Password     string
    MaxConns     int32
    MinConns     int32
    MaxConnLife  time.Duration
    MaxConnIdle  time.Duration
}

func NewPool(ctx context.Context, cfg Config) (*pgxpool.Pool, error) {
    connString := fmt.Sprintf(
        "host=%s port=%d dbname=%s user=%s password=%s pool_max_conns=%d pool_min_conns=%d",
        cfg.Host, cfg.Port, cfg.Database, cfg.User, cfg.Password,
        cfg.MaxConns, cfg.MinConns,
    )
    
    poolConfig, err := pgxpool.ParseConfig(connString)
    if err != nil {
        return nil, fmt.Errorf("failed to parse pool config: %w", err)
    }
    
    // Configure connection pool behavior
    poolConfig.MaxConnLifetime = cfg.MaxConnLife
    poolConfig.MaxConnIdleTime = cfg.MaxConnIdle
    poolConfig.HealthCheckPeriod = 1 * time.Minute
    
    pool, err := pgxpool.NewWithConfig(ctx, poolConfig)
    if err != nil {
        return nil, fmt.Errorf("failed to create connection pool: %w", err)
    }
    
    // Verify connectivity
    if err := pool.Ping(ctx); err != nil {
        pool.Close()
        return nil, fmt.Errorf("failed to ping database: %w", err)
    }
    
    return pool, nil
}
```

#### 4. **Repository Implementation (Using sqlc)**

```go
package repository

import (
    "context"
    "errors"
    "fmt"
    "time"
    
    "github.com/google/uuid"
    "github.com/jackc/pgx/v5"
    "github.com/jackc/pgx/v5/pgxpool"
    
    "lamp-control-api/internal/domain"
)

// PostgresLampRepository wraps sqlc Queries for domain-specific operations
type PostgresLampRepository struct {
    queries *Queries  // Generated by sqlc
    pool    *pgxpool.Pool
}

func NewPostgresLampRepository(pool *pgxpool.Pool) *PostgresLampRepository {
    return &PostgresLampRepository{
        queries: New(pool),  // sqlc-generated constructor
        pool:    pool,
    }
}

func (r *PostgresLampRepository) Create(ctx context.Context, lamp domain.Lamp) (domain.Lamp, error) {
    now := time.Now()
    
    // Use sqlc-generated method (compile-time safe)
    created, err := r.queries.CreateLamp(ctx, CreateLampParams{
        ID:        lamp.ID,
        IsOn:      lamp.IsOn,
        CreatedAt: now,
        UpdatedAt: now,
    })
    
    if err != nil {
        return domain.Lamp{}, fmt.Errorf("failed to create lamp: %w", err)
    }
    
    // Map sqlc type to domain type
    return domain.Lamp{
        ID:        created.ID,
        IsOn:      created.IsOn,
        CreatedAt: created.CreatedAt,
        UpdatedAt: created.UpdatedAt,
        DeletedAt: created.DeletedAt,
    }, nil
}

func (r *PostgresLampRepository) GetByID(ctx context.Context, id uuid.UUID) (domain.Lamp, error) {
    // Use sqlc-generated method (compile-time safe)
    lamp, err := r.queries.GetLampByID(ctx, id)
    
    if err != nil {
        if errors.Is(err, pgx.ErrNoRows) {
            return domain.Lamp{}, domain.ErrLampNotFound
        }
        return domain.Lamp{}, fmt.Errorf("failed to get lamp: %w", err)
    }
    
    // Map sqlc type to domain type
    return domain.Lamp{
        ID:        lamp.ID,
        IsOn:      lamp.IsOn,
        CreatedAt: lamp.CreatedAt,
        UpdatedAt: lamp.UpdatedAt,
        DeletedAt: lamp.DeletedAt,
    }, nil
}

func (r *PostgresLampRepository) GetAll(ctx context.Context, offset, limit int) ([]domain.Lamp, error) {
    // Use sqlc-generated method (compile-time safe)
    sqlcLamps, err := r.queries.ListLamps(ctx, ListLampsParams{
        Limit:  int32(limit),
        Offset: int32(offset),
    })
    
    if err != nil {
        return nil, fmt.Errorf("failed to query lamps: %w", err)
    }
    
    // Map sqlc types to domain types
    lamps := make([]domain.Lamp, 0, len(sqlcLamps))
    for _, sqlcLamp := range sqlcLamps {
        lamps = append(lamps, domain.Lamp{
            ID:        sqlcLamp.ID,
            IsOn:      sqlcLamp.IsOn,
            CreatedAt: sqlcLamp.CreatedAt,
            UpdatedAt: sqlcLamp.UpdatedAt,
            DeletedAt: sqlcLamp.DeletedAt,
        })
    }
    
    return lamps, nil
}

func (r *PostgresLampRepository) Update(ctx context.Context, id uuid.UUID, lamp domain.Lamp) (domain.Lamp, error) {
    now := time.Now()
    
    // Use sqlc-generated method (compile-time safe)
    updated, err := r.queries.UpdateLamp(ctx, UpdateLampParams{
        ID:        id,
        IsOn:      lamp.IsOn,
        UpdatedAt: now,
    })
    
    if err != nil {
        if errors.Is(err, pgx.ErrNoRows) {
            return domain.Lamp{}, domain.ErrLampNotFound
        }
        return domain.Lamp{}, fmt.Errorf("failed to update lamp: %w", err)
    }
    
    // Map sqlc type to domain type
    return domain.Lamp{
        ID:        updated.ID,
        IsOn:      updated.IsOn,
        CreatedAt: updated.CreatedAt,
        UpdatedAt: updated.UpdatedAt,
        DeletedAt: updated.DeletedAt,
    }, nil
}

func (r *PostgresLampRepository) Delete(ctx context.Context, id uuid.UUID) error {
    // Use sqlc-generated method (compile-time safe)
    rowsAffected, err := r.queries.DeleteLamp(ctx, DeleteLampParams{
        ID:        id,
        DeletedAt: time.Now(),
    })
    
    if err != nil {
        return fmt.Errorf("failed to delete lamp: %w", err)
    }
    
    if rowsAffected == 0 {
        return domain.ErrLampNotFound
    }
    
    return nil
}

// Health check
func (r *PostgresLampRepository) Ping(ctx context.Context) error {
    return r.pool.Ping(ctx)
}
```

#### 3. **Domain Model**

```go
package domain

import (
    "errors"
    "time"
    
    "github.com/google/uuid"
)

type Lamp struct {
    ID        uuid.UUID  `json:"id"`
    IsOn      bool       `json:"isOn"`
    CreatedAt time.Time  `json:"createdAt"`
    UpdatedAt time.Time  `json:"updatedAt"`
    DeletedAt *time.Time `json:"deletedAt,omitempty"`
}

var (
    ErrLampNotFound = errors.New("lamp not found")
)
```

#### 4. **Main Application Setup**

```go
package main

import (
    "context"
    "fmt"
    "log"
    "os"
    "time"
    
    "lamp-control-api/internal/repository"
    "lamp-control-api/internal/handlers"
)

func main() {
    ctx := context.Background()
    
    // Database configuration
    dbConfig := repository.Config{
        Host:        getEnv("DB_HOST", "localhost"),
        Port:        getEnvInt("DB_PORT", 5432),
        Database:    getEnv("DB_NAME", "lampcontrol"),
        User:        getEnv("DB_USER", "lampuser"),
        Password:    getEnv("DB_PASSWORD", "lamppass"),
        MaxConns:    int32(getEnvInt("DB_POOL_MAX_SIZE", 25)),
        MinConns:    int32(getEnvInt("DB_POOL_MIN_SIZE", 5)),
        MaxConnLife: time.Hour,
        MaxConnIdle: 30 * time.Minute,
    }
    
    // Create connection pool
    pool, err := repository.NewPool(ctx, dbConfig)
    if err != nil {
        log.Fatalf("Failed to create database pool: %v", err)
    }
    defer pool.Close()
    
    log.Println("Successfully connected to PostgreSQL")
    
    // Create repository
    lampRepo := repository.NewPostgresLampRepository(pool)
    
    // Create handlers with repository
    lampHandler := handlers.NewLampHandler(lampRepo)
    
    // Setup router and start server...
}

func getEnv(key, defaultValue string) string {
    if value := os.Getenv(key); value != "" {
        return value
    }
    return defaultValue
}
```

### Configuration

#### **Environment Variables**

```bash
# Database connection
DB_HOST=localhost
DB_PORT=5432
DB_NAME=lampcontrol
DB_USER=lampuser
DB_PASSWORD=lamppass

# Connection pool
DB_POOL_MAX_SIZE=25
DB_POOL_MIN_SIZE=5

# Or use connection string
DATABASE_URL=postgres://lampuser:lamppass@localhost:5432/lampcontrol?pool_max_conns=25&pool_min_conns=5
```

### Dependencies

```go
// go.mod
module lamp-control-api

go 1.22

require (
    github.com/go-chi/chi/v5 v5.2.1
    github.com/google/uuid v1.6.0
    github.com/jackc/pgx/v5 v5.5.1
)

require (
    github.com/jackc/pgpassfile v1.0.0 // indirect
    github.com/jackc/pgservicefile v0.0.0-20231201235250-de7065d80cb9 // indirect
    github.com/jackc/puddle/v2 v2.2.1 // indirect
    golang.org/x/crypto v0.17.0 // indirect
    golang.org/x/sync v0.1.0 // indirect
    golang.org/x/text v0.14.0 // indirect
)
```

**Development Tool:**
```bash
# Install sqlc for code generation
go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest

# Verify installation
sqlc version
```

### Migration Strategy

#### **Option 1: Apply Existing Schema (Recommended)**

```bash
# Install golang-migrate
go install -tags 'postgres' github.com/golang-migrate/migrate/v4/cmd/migrate@latest

# Create migrations directory
mkdir -p migrations

# Copy existing schema as initial migration
cat << 'EOF' > migrations/000001_initial_schema.up.sql
-- PostgreSQL Schema for Lamp Control API
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_lamps_is_on ON lamps (is_on);
CREATE INDEX IF NOT EXISTS idx_lamps_created_at ON lamps (created_at);
CREATE INDEX IF NOT EXISTS idx_lamps_deleted_at ON lamps (deleted_at);

CREATE OR REPLACE FUNCTION UPDATE_UPDATED_AT_COLUMN()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_lamps_updated_at
BEFORE UPDATE ON lamps
FOR EACH ROW
EXECUTE FUNCTION UPDATE_UPDATED_AT_COLUMN();
EOF

# Create down migration
cat << 'EOF' > migrations/000001_initial_schema.down.sql
DROP TRIGGER IF EXISTS update_lamps_updated_at ON lamps;
DROP FUNCTION IF EXISTS UPDATE_UPDATED_AT_COLUMN();
DROP TABLE IF EXISTS lamps;
DROP EXTENSION IF EXISTS "uuid-ossp";
EOF

# Apply migration
migrate -path migrations -database "postgres://lampuser:lamppass@localhost:5432/lampcontrol?sslmode=disable" up
```

#### **Option 2: Embed Migrations in Application**

```go
package main

import (
    "embed"
    
    "github.com/golang-migrate/migrate/v4"
    _ "github.com/golang-migrate/migrate/v4/database/postgres"
    "github.com/golang-migrate/migrate/v4/source/iofs"
)

//go:embed migrations/*.sql
var migrationsFS embed.FS

func runMigrations(databaseURL string) error {
    d, err := iofs.New(migrationsFS, "migrations")
    if err != nil {
        return err
    }
    
    m, err := migrate.NewWithSourceInstance("iofs", d, databaseURL)
    if err != nil {
        return err
    }
    
    return m.Up()
}
```

### Testing Strategy

#### **Integration Tests with Docker**

```go
package repository_test

import (
    "context"
    "testing"
    "time"
    
    "github.com/google/uuid"
    "github.com/stretchr/testify/assert"
    "github.com/stretchr/testify/require"
    "github.com/testcontainers/testcontainers-go"
    "github.com/testcontainers/testcontainers-go/postgres"
    "github.com/testcontainers/testcontainers-go/wait"
    
    "lamp-control-api/internal/domain"
    "lamp-control-api/internal/repository"
)

func setupTestDB(t *testing.T) (*repository.PostgresLampRepository, func()) {
    ctx := context.Background()
    
    // Start PostgreSQL container
    postgresContainer, err := postgres.RunContainer(ctx,
        testcontainers.WithImage("postgres:16-alpine"),
        postgres.WithDatabase("lampcontrol_test"),
        postgres.WithUsername("test"),
        postgres.WithPassword("test"),
        testcontainers.WithWaitStrategy(
            wait.ForLog("database system is ready to accept connections").
                WithOccurrence(2).
                WithStartupTimeout(5*time.Second)),
    )
    require.NoError(t, err)
    
    // Get connection string
    connStr, err := postgresContainer.ConnectionString(ctx, "sslmode=disable")
    require.NoError(t, err)
    
    // Parse and create pool
    poolConfig, err := pgxpool.ParseConfig(connStr)
    require.NoError(t, err)
    
    pool, err := pgxpool.NewWithConfig(ctx, poolConfig)
    require.NoError(t, err)
    
    // Apply schema
    _, err = pool.Exec(ctx, schemaSQL)
    require.NoError(t, err)
    
    repo := repository.NewPostgresLampRepository(pool)
    
    cleanup := func() {
        pool.Close()
        _ = postgresContainer.Terminate(ctx)
    }
    
    return repo, cleanup
}

func TestPostgresLampRepository_Create(t *testing.T) {
    repo, cleanup := setupTestDB(t)
    defer cleanup()
    
    ctx := context.Background()
    lamp := domain.Lamp{
        ID:   uuid.New(),
        IsOn: true,
    }
    
    created, err := repo.Create(ctx, lamp)
    
    require.NoError(t, err)
    assert.Equal(t, lamp.ID, created.ID)
    assert.True(t, created.IsOn)
    assert.False(t, created.CreatedAt.IsZero())
    assert.False(t, created.UpdatedAt.IsZero())
}

func TestPostgresLampRepository_GetByID(t *testing.T) {
    repo, cleanup := setupTestDB(t)
    defer cleanup()
    
    ctx := context.Background()
    
    // Create lamp
    lamp := domain.Lamp{ID: uuid.New(), IsOn: true}
    created, err := repo.Create(ctx, lamp)
    require.NoError(t, err)
    
    // Retrieve lamp
    retrieved, err := repo.GetByID(ctx, created.ID)
    
    require.NoError(t, err)
    assert.Equal(t, created.ID, retrieved.ID)
    assert.Equal(t, created.IsOn, retrieved.IsOn)
}

const schemaSQL = `
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);
`
```

### Performance Optimizations

#### **1. Batch Operations**

```go
func (r *PostgresLampRepository) CreateBatch(ctx context.Context, lamps []domain.Lamp) error {
    batch := &pgx.Batch{}
    
    query := `
        INSERT INTO lamps (id, is_on, created_at, updated_at)
        VALUES ($1, $2, $3, $4)
    `
    
    now := time.Now()
    for _, lamp := range lamps {
        batch.Queue(query, lamp.ID, lamp.IsOn, now, now)
    }
    
    results := r.pool.SendBatch(ctx, batch)
    defer results.Close()
    
    for range lamps {
        if _, err := results.Exec(); err != nil {
            return fmt.Errorf("batch insert failed: %w", err)
        }
    }
    
    return nil
}
```

#### **2. COPY Protocol for Bulk Inserts**

```go
func (r *PostgresLampRepository) BulkInsert(ctx context.Context, lamps []domain.Lamp) error {
    _, err := r.pool.CopyFrom(
        ctx,
        pgx.Identifier{"lamps"},
        []string{"id", "is_on", "created_at", "updated_at"},
        pgx.CopyFromSlice(len(lamps), func(i int) ([]any, error) {
            now := time.Now()
            return []any{lamps[i].ID, lamps[i].IsOn, now, now}, nil
        }),
    )
    return err
}
```

#### **3. Prepared Statements**

```go
// pgx automatically caches prepared statements
// No manual preparation needed for repeated queries
```

## Rationale

### Why pgx v5?

1. **Performance**: 3-5x faster than database/sql for PostgreSQL operations
2. **Pure Go**: No CGO dependencies, easier deployment, better debugging
3. **Type Safety**: Native support for PostgreSQL types (UUID, arrays, JSONB, ranges)
4. **Modern API**: Context-aware, async-friendly, connection pooling built-in
5. **Active Development**: Regular updates, PostgreSQL 16+ feature support
6. **Production Proven**: Used by major companies (Uber, GitHub, Discord)
7. **Advanced Features**: LISTEN/NOTIFY, COPY protocol, query batching

### Why Not sqlx?

- **PostgreSQL-Specific**: pgx is optimized specifically for PostgreSQL
- **Performance**: sqlx uses database/sql, which is slower
- **Features**: pgx has PostgreSQL-specific features (LISTEN/NOTIFY, COPY)
- **Trade-off**: sqlx is better for multi-database applications

### Why Not GORM?

- **Overhead**: 2-3x slower than pgx
- **Complexity**: Full ORM is overkill for simple CRUD APIs
- **Magic**: Too much abstraction, harder to debug
- **Use Case**: Better for complex domain models with many relationships

## Consequences

### Positive

- ✅ **Maximum Performance**: Fastest PostgreSQL driver for Go (pgx)
- ✅ **Compile-Time Type Safety**: sqlc catches SQL errors at compile time
- ✅ **Zero Runtime Overhead**: sqlc generates code, no reflection
- ✅ **Refactoring Safety**: Schema changes break compilation
- ✅ **Context Support**: Native cancellation and timeout handling
- ✅ **Connection Pooling**: Built-in pgxpool with excellent defaults
- ✅ **Testability**: Easy integration testing with Testcontainers
- ✅ **No Manual Scanning**: sqlc auto-generates type-safe methods

### Negative

- ❌ **PostgreSQL-Only**: Not portable to other databases
- ❌ **Build Step**: Requires `sqlc generate` before compilation
- ❌ **No Migrations**: Must use external tools (golang-migrate)
- ❌ **Learning Curve**: Must learn SQL annotations and sqlc workflow
- ❌ **Tooling Required**: Need to install sqlc CLI tool
- ❌ **SQL Knowledge**: Still requires writing SQL (not hidden by ORM)

### Neutral

- 🔄 **Migration Management**: Must choose migration strategy (embedded vs CLI)
- 🔄 **Generated Code**: Need to commit sqlc-generated files or run in CI
- 🔄 **Connection Tuning**: Must configure pool size based on workload
- 🔄 **Dynamic Queries**: For complex dynamic queries, may need squirrel library

## Implementation Checklist

- [ ] Install sqlc: `go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest`
- [ ] Add pgx/v5 and pgxpool dependencies
- [ ] Create `sqlc.yaml` configuration file
- [ ] Write SQL queries with sqlc annotations in `queries/lamps.sql`
- [ ] Run `sqlc generate` to create type-safe code
- [ ] Create `repository.Config` struct for database configuration
- [ ] Implement `NewPool()` function with connection pooling
- [ ] Create `PostgresLampRepository` struct wrapping sqlc Queries
- [ ] Implement CRUD methods using sqlc-generated functions
- [ ] Add error handling and domain error mapping
- [ ] Configure environment variables for database connection
- [ ] Add health check endpoint using `pool.Ping()`
- [ ] Create migrations directory with initial schema
- [ ] Write integration tests with Testcontainers
- [ ] Update README with sqlc and database setup instructions
- [ ] Add Makefile targets for `sqlc generate` and migrations

## References

- [pgx Documentation](https://github.com/jackc/pgx)
- [pgxpool Documentation](https://pkg.go.dev/github.com/jackc/pgx/v5/pgxpool)
- [sqlc Documentation](https://docs.sqlc.dev/)
- [sqlc GitHub](https://github.com/sqlc-dev/sqlc)
- [golang-migrate](https://github.com/golang-migrate/migrate)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [Testcontainers Go](https://golang.testcontainers.org/)

## Related ADRs

- [ADR 001: Go Version Selection](001-go-version-selection.md)
- [ADR 002: HTTP Router Selection](002-http-router-selection.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
