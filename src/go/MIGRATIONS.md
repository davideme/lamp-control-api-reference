# Database Migrations for Go Implementation

This Go implementation uses **golang-migrate** for automated database schema migrations.

## Overview

golang-migrate is a database migration tool that applies versioned SQL migrations to your PostgreSQL database automatically on application startup. Migrations are embedded into the binary for easy deployment.

## Migration Files

Migrations are located in `api/migrations/` and follow golang-migrate's naming convention. Each migration has two files:

- `{version}_{description}.up.sql` - Applied when migrating forward
- `{version}_{description}.down.sql` - Applied when rolling back

### Current Migrations

1. **000001_initial_schema** - Creates the base `lamps` table with UUID, status, and timestamp columns
2. **000002_add_soft_deletes** - Adds the `deleted_at` column for soft delete functionality
3. **000003_add_updated_at_trigger** - Creates PostgreSQL trigger for automatic `updated_at` management

## How It Works

1. **On Application Startup**: `main.go` calls `api.RunMigrations()` before creating the connection pool
2. **Embedded Migrations**: Migration files are embedded into the binary using Go's `embed` package
3. **Migration Check**: golang-migrate compares applied migrations (tracked in `schema_migrations` table) with available migration files
4. **Apply Pending**: Any new migrations are executed in version order
5. **Connection Pool Created**: Only after successful migrations, the pgxpool connection is established

## Configuration

Migrations are configured in `api/migrate.go`:

- **Source**: Embedded filesystem (`migrations/*.sql`)
- **Driver**: PostgreSQL driver
- **Error Handling**: Falls back to in-memory repository on migration failure (unless `--require-db` flag is set)

## Dependencies

Added to `go.mod`:

```go
github.com/golang-migrate/migrate/v4
github.com/golang-migrate/migrate/v4/database/postgres
github.com/golang-migrate/migrate/v4/source/iofs
```

## Creating New Migrations

To create a new migration:

1. Create two new SQL files in the `api/migrations/` directory:
   ```
   api/migrations/000004_add_new_feature.up.sql
   api/migrations/000004_add_new_feature.down.sql
   ```

2. Write your forward migration in the `.up.sql` file:
   ```sql
   ALTER TABLE lamps ADD COLUMN brightness INT DEFAULT 100;
   ```

3. Write your rollback migration in the `.down.sql` file:
   ```sql
   ALTER TABLE lamps DROP COLUMN brightness;
   ```

4. Restart the application - migrations will be detected and applied automatically

## Migration Execution Order

Migrations are applied in numerical order:
- 000001 → 000002 → 000003 → ... → 00000n

## Rollback

To rollback migrations:

1. **Using migrate CLI** (if installed):
   ```bash
   migrate -path api/migrations -database "postgres://user:pass@localhost:5432/dbname?sslmode=disable" down 1
   ```

2. **Manual rollback**:
   - Apply the `.down.sql` script manually
   - Update the `schema_migrations` table to reflect the new version

## Embedded Migrations

Migrations are embedded into the binary using Go 1.16+ embed directive:

```go
//go:embed migrations/*.sql
var migrationsFS embed.FS
```

This means:
- ✅ No need to distribute migration files separately
- ✅ Migrations are versioned with your code
- ✅ Single binary deployment
- ✅ Consistent migrations across environments

## Troubleshooting

### Migration Failed

If a migration fails:
1. Check application logs for the specific error
2. Manually inspect the `schema_migrations` table
3. If a migration is in "dirty" state, clean it up:
   ```sql
   UPDATE schema_migrations SET dirty = false WHERE version = <failed_version>;
   ```
4. Fix the SQL in the migration file
5. Restart the application

### Dirty Migration State

A "dirty" migration means golang-migrate detected that a migration started but didn't complete successfully:
- The database might be in a partially migrated state
- You must manually verify and fix the database schema
- Update the `schema_migrations` table to mark it as clean

### Connection String Format

golang-migrate requires PostgreSQL connection strings in URL format, using either `postgres://...` or `postgresql://...`:
```
postgres://user:password@host:port/database?sslmode=disable
postgresql://user:password@host:port/database?sslmode=disable
```

The application automatically converts component-based connection strings to this format.

## Comparison with Other Implementations

| Language | Migration Tool | Status |
|----------|---------------|---------|
| Java | Flyway | ✅ Already implemented |
| Kotlin | Flyway | ✅ Already implemented |
| Python | Alembic | ✅ Already implemented |
| TypeScript | Prisma | ✅ Already implemented |
| C# | EF Core | ✅ Already implemented |
| **Go** | **golang-migrate** | ✅ **This PR** |

With this golang-migrate integration, Go now has automated migration capabilities matching all other implementations!

## Benefits

- ✅ **Automated schema versioning** - No manual schema deployment
- ✅ **Embedded in binary** - Single artifact deployment
- ✅ **Forward and backward** - Support for rollbacks via .down.sql files
- ✅ **Migration history** - Tracked in `schema_migrations` table
- ✅ **Validation** - Detects dirty/failed migrations
- ✅ **Type-safe** - Go compilation ensures migration files exist
