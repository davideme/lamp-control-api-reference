# Database Migrations for Kotlin Implementation

This Kotlin implementation uses **Flyway** for automated database schema migrations.

## Overview

Flyway is a database migration tool that applies versioned SQL migrations to your PostgreSQL database automatically on application startup.

## Migration Files

Migrations are located in `src/main/resources/db/migration/` and follow Flyway's naming convention:

- `V1__Initial_schema.sql` - Creates the base `lamps` table with UUID, status, and timestamp columns
- `V2__Add_soft_deletes.sql` - Adds the `deleted_at` column for soft delete functionality
- `V3__Add_updated_at_trigger.sql` - Creates PostgreSQL trigger for automatic `updated_at` management

## How It Works

1. **On Application Startup**: `DatabaseFactory.init()` calls `FlywayConfig.runMigrations()`
2. **Flyway Checks**: Flyway compares applied migrations (tracked in `flyway_schema_history` table) with available migration files
3. **Apply Pending Migrations**: Any new migrations are executed in order
4. **Validation**: Flyway validates that applied migrations haven't been modified

## Configuration

Flyway is configured in `FlywayConfig.kt` with the following settings:

- **Locations**: `classpath:db/migration`
- **Baseline on Migrate**: `true` (allows Flyway to baseline existing databases)
- **Validate on Migrate**: `true` (validates migration checksums)

## Dependencies

Added to `build.gradle.kts`:

```kotlin
implementation("org.flywaydb:flyway-core:10.21.0")
implementation("org.flywaydb:flyway-database-postgresql:10.21.0")
```

## Creating New Migrations

To create a new migration:

1. Create a new SQL file in `src/main/resources/db/migration/`
2. Follow naming convention: `V<VERSION>__<Description>.sql`
   - Example: `V4__Add_user_table.sql`
3. Write your SQL migration
4. Restart the application - Flyway will detect and apply it automatically

## Migration Execution Order

Migrations are applied in version order:
1. V1 → V2 → V3 → ... → Vn

## Rollback

Flyway Community Edition (which we use) does not support automatic rollbacks. To rollback:

1. Create a new migration that reverses the changes
2. Or manually revert the database and remove the migration entry from `flyway_schema_history`

## Troubleshooting

### Migration Failed

If a migration fails:
- Check logs for the specific error
- Fix the SQL in the migration file
- Manually clean up any partial changes in the database
- Delete the failed migration record from `flyway_schema_history` table
- Restart the application

### Checksum Mismatch

If you see checksum validation errors:
- This means a migration file was modified after being applied
- **Never modify applied migrations** - create a new migration instead
- To resolve: `flyway repair` (requires Flyway CLI) or manually update the checksum in `flyway_schema_history`

## Comparison with Other Implementations

- **Java**: Also uses Flyway (Spring Boot auto-configuration)
- **Python**: Uses Alembic (Python-specific migration tool)
- **TypeScript**: Uses Prisma Migrate (schema-first approach)
- **C#**: Uses Entity Framework Migrations (code-first approach)
- **Go**: Manual migrations (shared schema file)

With this Flyway integration, Kotlin now has the same automated migration capabilities as Java!
