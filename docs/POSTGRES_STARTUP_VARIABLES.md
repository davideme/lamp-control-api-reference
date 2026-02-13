# PostgreSQL Setup Guide (Per Language)

Use this as a quick reference when you want each implementation to run with PostgreSQL instead of in-memory storage.

## Quick Start

1. Pick the language implementation you want to run.
2. Export the variables listed for that language.
3. Start the app in your usual mode (`serve-only`, `serve`, or `migrate` where supported).

## TypeScript (`src/typescript`)

Set:

```bash
export DATABASE_URL='postgresql://<user>:<password>@<host>:5432/<database>?schema=public'
```

Notes:
- PostgreSQL is enabled when `DATABASE_URL` is set.
- If `DATABASE_URL` is unset, TypeScript uses in-memory storage.

## Python (`src/python`)

Set:

```bash
export DATABASE_URL='postgresql://<user>:<password>@<host>:5432/<database>'
```

Optional pool tuning:

```bash
export DB_POOL_MIN_SIZE='5'
export DB_POOL_MAX_SIZE='20'
```

Notes:
- PostgreSQL is enabled when `DATABASE_URL` is set.
- `DB_*` variables alone do not switch Python to PostgreSQL mode.

## Java (`src/java`)

Set:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://<host>:5432/<database>'
export DB_USER='<user>'
export DB_PASSWORD='<password>'
```

Alternative:

```bash
export DATABASE_URL='jdbc:postgresql://<host>:5432/<database>'
export DB_USER='<user>'
export DB_PASSWORD='<password>'
```

Notes:
- URL must be JDBC format (`jdbc:postgresql://...`).
- A plain `postgresql://...` URL will not work for Java datasource config.

## C# (`src/csharp`)

Set:

```bash
export ConnectionStrings__LampControl='Host=<host>;Port=5432;Database=<database>;Username=<user>;Password=<password>'
```

Notes:
- PostgreSQL is enabled when `ConnectionStrings__LampControl` is set.
- `DATABASE_URL` is not used by C# runtime config.

## Go (`src/go`)

Recommended:

```bash
export DATABASE_URL='postgres://<user>:<password>@<host>:5432/<database>?sslmode=disable'
```

Alternative (component vars):

```bash
export DB_HOST='<host>'
export DB_PORT='5432'
export DB_NAME='<database>'
export DB_USER='<user>'
export DB_PASSWORD='<password>'
```

Optional pool tuning:

```bash
export DB_POOL_MIN_SIZE='0'
export DB_POOL_MAX_SIZE='4'
```

Notes:
- `DATABASE_URL` takes precedence over component vars.

## Kotlin (`src/kotlin`)

Recommended:

```bash
export DATABASE_URL='postgresql://<user>:<password>@<host>:5432/<database>'
```

Alternative (component vars):

```bash
export DB_HOST='<host>'
export DB_PORT='5432'
export DB_NAME='<database>'
export DB_USER='<user>'
export DB_PASSWORD='<password>'
```

Optional pool/timeout tuning:

```bash
export DB_POOL_MIN_SIZE='0'
export DB_POOL_MAX_SIZE='4'
export DB_MAX_LIFETIME_MS='3600000'
export DB_IDLE_TIMEOUT_MS='1800000'
export DB_CONNECTION_TIMEOUT_MS='30000'
```

Notes:
- If you use `DATABASE_URL`, keep it in standard `postgresql://...` or `postgres://...` form.

## Common Gotchas

- Java needs `jdbc:postgresql://...`; others usually use `postgresql://...` or `postgres://...`.
- C# uses `ConnectionStrings__LampControl`, not `DATABASE_URL`.
- If an app still runs in memory mode, first verify the exact variable name is exported in the same shell/session used to start the app.
