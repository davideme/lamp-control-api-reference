# PostgreSQL Startup Variables by Implementation

This document summarizes how each implementation under `src/` decides whether to use PostgreSQL and which environment variable formats it expects.

Synced with `main` and re-validated against current sources on 2026-02-13.

## Cross-Implementation Note

- `USE_POSTGRES` should not be used as a PostgreSQL mode switch. Current runtime code paths in `src/` use connection-related variables (for example `DATABASE_URL`, JDBC URL, or `ConnectionStrings__LampControl`) to decide storage backend.

## TypeScript (`src/typescript`)

Source of truth:
- `src/typescript/src/infrastructure/app.ts`
- `src/typescript/src/infrastructure/database/client.ts`
- `src/typescript/src/cli.ts`

PostgreSQL is enabled when:
- `DATABASE_URL` is set and non-empty.

Required variables:
- `DATABASE_URL`

Expected format:
- Prisma PostgreSQL URL, for example:
  - `postgresql://user:password@host:5432/database`
  - Optional query params are supported by Prisma (example: `?schema=public`).

Notes:
- `USE_POSTGRES` is not read by runtime code in `src/typescript/src/*`.
- If `DATABASE_URL` is missing, app uses in-memory repository.
- In `--mode=serve` and `--mode=migrate`, migrations run only if `DATABASE_URL` is present.

## Python (`src/python`)

Source of truth:
- `src/python/src/openapi_server/infrastructure/config.py`
- `src/python/src/openapi_server/dependencies.py`
- `src/python/src/openapi_server/cli.py`

PostgreSQL is enabled when:
- `DATABASE_URL` is set and not blank.

Required variables:
- `DATABASE_URL`

Expected format:
- Preferred input: `postgresql://user:password@host:5432/database`
- Runtime converts it to async driver format: `postgresql+asyncpg://...`
- If already provided as `postgresql+asyncpg://...`, it is used directly.
- `sslmode` query parameter is removed automatically from `DATABASE_URL`.

Optional variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `DB_POOL_MIN_SIZE`, `DB_POOL_MAX_SIZE`

Important caveat:
- Individual `DB_*` variables alone do **not** switch to PostgreSQL mode.
- PostgreSQL mode switch depends specifically on `DATABASE_URL` presence.

## Java (`src/java`)

Source of truth:
- `src/java/src/main/resources/application.properties`
- `src/java/src/main/java/org/openapitools/config/OnDatabaseUrlCondition.java`
- `src/java/src/main/java/org/openapitools/config/DataSourceConfig.java`

PostgreSQL is enabled when:
- `spring.datasource.url` resolves to a non-empty value.
- Resolution order in properties:
  - `SPRING_DATASOURCE_URL`
  - then `DATABASE_URL`

Required variable:
- `SPRING_DATASOURCE_URL` or `DATABASE_URL`

Expected format:
- Must be JDBC URL format:
  - `jdbc:postgresql://host:5432/database`

Optional variables:
- `DB_USER` (default: `lampuser`)
- `DB_PASSWORD` (default: `lamppass`)
- `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_SIZE`
- `FLYWAY_ENABLED` (used for migration behavior)

Important caveat:
- A non-JDBC URL like `postgresql://...` is not valid for `spring.datasource.url`.

## C# (`src/csharp`)

Source of truth:
- `src/csharp/LampControlApi/Extensions/ServiceCollectionExtensions.cs`
- `src/csharp/LampControlApi/Extensions/MigrationRunner.cs`
- `src/csharp/LampControlApi/appsettings.Development.example.json`

PostgreSQL is enabled when:
- Connection string `ConnectionStrings:LampControl` is non-empty.
- Resolution order:
  - config key `ConnectionStrings:LampControl`
  - fallback env var `ConnectionStrings__LampControl`

Required variable (if using env):
- `ConnectionStrings__LampControl`

Expected format:
- Npgsql connection string, for example:
  - `Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass`

Notes:
- `DATABASE_URL` is not used by the C# implementation.

## Go (`src/go`)

Source of truth:
- `src/go/api/config.go`
- `src/go/cmd/lamp-control-api/main.go`

PostgreSQL is enabled when **any** of these is true:
- `DATABASE_URL` is set, or
- `DB_NAME` is set, or
- both `DB_HOST` and `DB_USER` are set.

Primary variable:
- `DATABASE_URL` (takes precedence if set)

Expected format:
- Recommended: URL form, e.g. `postgres://user:password@host:5432/database?sslmode=disable`
- Also supports pgx key-value DSN internally (built when `DATABASE_URL` is not set):
  - `host=... port=... dbname=... user=... password=...`

Optional variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `DB_POOL_MIN_SIZE`, `DB_POOL_MAX_SIZE`

## Kotlin (`src/kotlin`)

Source of truth:
- `src/kotlin/src/main/kotlin/com/lampcontrol/database/DatabaseFactory.kt`
- `src/kotlin/src/main/kotlin/com/lampcontrol/Application.kt`

PostgreSQL is enabled when **any** of these is true:
- `DATABASE_URL` is set, or
- `DB_NAME` is set, or
- both `DB_HOST` and `DB_USER` are set.

Primary variable:
- `DATABASE_URL` (preferred when available)

Expected `DATABASE_URL` format:
- Strictly parsed by regex:
  - `postgresql://user:password@host:5432/database`
  - or `postgres://user:password@host:5432/database`

Important caveat:
- Query parameters (for example `?sslmode=...`) are not handled by the current parser and can fail parsing.

Optional variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `DB_POOL_MIN_SIZE`, `DB_POOL_MAX_SIZE`
- `DB_MAX_LIFETIME_MS`, `DB_IDLE_TIMEOUT_MS`, `DB_CONNECTION_TIMEOUT_MS`

## Quick Reference Matrix

| Language | Switch to PostgreSQL | Required variable(s) | Connection string format |
|---|---|---|---|
| TypeScript | `DATABASE_URL` non-empty | `DATABASE_URL` | `postgresql://...` (Prisma URL) |
| Python | `DATABASE_URL` non-empty | `DATABASE_URL` | `postgresql://...` or `postgresql+asyncpg://...` |
| Java | `spring.datasource.url` non-empty | `SPRING_DATASOURCE_URL` or `DATABASE_URL` | `jdbc:postgresql://...` |
| C# | `ConnectionStrings:LampControl` non-empty | `ConnectionStrings__LampControl` (env) | `Host=...;Port=...;Database=...;Username=...;Password=...` |
| Go | `DATABASE_URL` or `DB_NAME` or (`DB_HOST`+`DB_USER`) | `DATABASE_URL` recommended | `postgres://...` recommended; key-value DSN supported |
| Kotlin | `DATABASE_URL` or `DB_NAME` or (`DB_HOST`+`DB_USER`) | `DATABASE_URL` recommended | `postgresql://...` or `postgres://...` (strict parser) |
