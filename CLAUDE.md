# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-language reference implementation of a simple Lamp Control API, showcasing how to build the same REST API in 6 different languages (TypeScript, Python, Java, C#, Go, Kotlin). The project demonstrates:

- Consistent API design across languages using OpenAPI 3.0 specification
- Database integration (PostgreSQL primary, MySQL/MongoDB support)
- Testing strategies and code quality metrics
- Three operation modes: migrate (migrations only), serve-only (server only), serve (migrations + server)

**Key Principle**: The simple lamp resource (ID + on/off state) keeps focus on implementation patterns rather than complex business logic.

## Repository Structure

```
lamp-control-api-reference/
├── src/                    # Language implementations
│   ├── typescript/         # Node.js + Fastify + Prisma
│   ├── python/            # FastAPI + SQLAlchemy
│   ├── java/              # Spring Boot + JPA + Flyway
│   ├── csharp/            # ASP.NET Core + EF Core
│   ├── go/                # Chi Router + sqlc
│   └── kotlin/            # Ktor + Exposed
├── docs/api/              # Shared OpenAPI specification
│   └── openapi.yaml       # Source of truth for all implementations
├── database/              # Database schemas (MySQL, PostgreSQL, MongoDB)
├── scripts/               # Testing and CI scripts
│   ├── ci/                # Mode testing scripts
│   └── extract-coverage.sh
└── tools/                 # Code metrics tools
```

## Common Development Commands

### Running Locally (Quick Start)

Each language uses the **serve** mode for local development (runs migrations + starts server):

```bash
# TypeScript (port 3000)
cd src/typescript
npm install
npm run dev

# Python (port 8000)
cd src/python
poetry install
poetry run uvicorn openapi_server.main:app --reload

# Go (port 8080)
cd src/go
make dev

# Java (port 8081)
cd src/java
mvn spring-boot:run

# Kotlin (port 8082)
cd src/kotlin
./gradlew run

# C# (port 8083)
cd src/csharp/LampControlApi
dotnet run
```

### Building

```bash
# TypeScript
cd src/typescript && npm run build

# Python
cd src/python && poetry build

# Go
cd src/go && make build

# Java
cd src/java && mvn clean install

# Kotlin
cd src/kotlin && ./gradlew build

# C#
cd src/csharp && dotnet build --configuration Release
```

### Testing

```bash
# TypeScript
cd src/typescript && npm test                    # Run tests
cd src/typescript && npm run test:coverage       # With coverage

# Python
cd src/python && poetry run pytest               # Run tests
cd src/python && poetry run pytest --cov         # With coverage

# Go
cd src/go && make test                           # Run tests
cd src/go && make test-coverage                  # With coverage report

# Java
cd src/java && mvn test                          # Run tests
cd src/java && mvn test jacoco:report            # With coverage

# Kotlin
cd src/kotlin && ./gradlew test                  # Run tests
cd src/kotlin && ./gradlew test jacocoTestReport # With coverage

# C#
cd src/csharp && dotnet test                                          # Run tests
cd src/csharp && make test-coverage                                   # With coverage
```

### Linting and Formatting

```bash
# TypeScript
cd src/typescript && npm run lint                # Check
cd src/typescript && npm run format              # Fix

# Python
cd src/python && poetry run black . && poetry run ruff check . --fix

# Go
cd src/go && make lint                           # Check all
cd src/go && make fmt                            # Fix formatting

# Java
cd src/java && mvn spotless:check                # Check
cd src/java && mvn spotless:apply                # Fix

# Kotlin
cd src/kotlin && ./gradlew ktlintCheck           # Check
cd src/kotlin && ./gradlew ktlintFormat          # Fix
cd src/kotlin && ./gradlew detekt                # Static analysis

# C#
cd src/csharp && dotnet format --verify-no-changes  # Check
cd src/csharp && dotnet format                      # Fix
```

## Architecture

### Domain-Driven Design Pattern

All implementations follow a layered architecture:

```
domain/           - Core business logic (entities, repositories interfaces)
├── entities/     - Domain models (Lamp)
├── repositories/ - Repository interfaces
└── errors/       - Domain-specific errors

infrastructure/   - Technical implementation details
├── database/     - Database connection, migrations
├── repositories/ - Repository implementations
├── mappers/      - Domain ↔ Database mapping
└── services/     - Framework-specific services (API routes, validation)
```

**Key principle**: Domain layer has NO dependencies on infrastructure. Infrastructure depends on domain.

### TypeScript Specifics
- **Fastify** for HTTP server (not Express)
- **Prisma** for PostgreSQL ORM
- **fastify-openapi-glue** binds OpenAPI spec to routes
- Generated types from OpenAPI: `npm run generate-types`

### Python Specifics
- **FastAPI** framework with automatic OpenAPI generation
- **SQLAlchemy 2.0** for database (async)
- **Alembic** for migrations
- **Poetry** for dependency management

### Go Specifics
- **Chi Router** for HTTP routing
- **sqlc** for type-safe SQL (not an ORM)
- Database migrations in `internal/database/migrations/`
- Strong emphasis on interfaces and dependency injection

### Java Specifics
- **Spring Boot 3** with dependency injection
- **JPA/Hibernate** for ORM
- **Flyway** for migrations
- **Maven** for build management
- Dual mode: in-memory repository (no DATABASE_URL) or JPA (with DATABASE_URL)

### Kotlin Specifics
- **Ktor** framework
- **Exposed** ORM framework
- **Gradle** with Kotlin DSL
- Coroutines for async operations

### C# Specifics
- **ASP.NET Core** with minimal APIs
- **Entity Framework Core** for database
- Built-in migration system
- **dotnet** CLI for all operations

## Database Setup

### Local Development with Docker Compose

```bash
# Start all databases (MySQL, PostgreSQL, MongoDB)
docker-compose up -d

# PostgreSQL is primary - most implementations use this
DATABASE_URL=postgresql://lamp_user:lamp_password@localhost:5432/lamp_control
```

### Database Schema

All databases share this structure:
- **id**: UUID primary key
- **is_on**: boolean status
- **created_at**: timestamp
- **updated_at**: timestamp
- **deleted_at**: timestamp (soft delete)

### Migrations

Each language manages migrations differently:
- **TypeScript**: Prisma (`npx prisma migrate dev`)
- **Python**: Alembic (`alembic upgrade head`)
- **Go**: Embedded SQL files in `internal/database/migrations/`
- **Java**: Flyway (auto-runs on startup in migrate/serve modes)
- **Kotlin**: Exposed migrations
- **C#**: EF Core (`dotnet ef database update`)

## Operation Modes

All implementations support three modes via CLI flags or environment variables:

1. **migrate**: Run migrations only, then exit (production phase 1)
2. **serve-only**: Start server without running migrations (production phase 2, DEFAULT)
3. **serve**: Run migrations AND start server (local development convenience)

### Why Three Modes?

**Production Pattern** (zero-downtime deployments):
1. Deploy new version in migrate mode → apply schema changes
2. Deploy multiple servers in serve-only mode → horizontal scaling

**Local Development**: Use serve mode for convenience (migrations + server in one step)

### Mode Testing

Test all three modes for a language:
```bash
cd scripts
./test-modes-local.sh <language>  # e.g., ./test-modes-local.sh go
```

## OpenAPI Specification

**Source of Truth**: `docs/api/openapi.yaml`

All implementations must conform to this spec. When modifying the API:

1. Update `docs/api/openapi.yaml` first
2. Regenerate language-specific code/types:
   ```bash
   # TypeScript
   cd src/typescript && npm run generate-types

   # Python (uses spec directly, no generation needed)

   # Java
   cd src/java && make generate

   # Kotlin
   cd src/kotlin && make generate
   ```
3. Update all 6 language implementations
4. Update tests

### API Endpoints

- `GET /v1/lamps` - List lamps (with cursor pagination)
- `POST /v1/lamps` - Create lamp
- `GET /v1/lamps/{lampId}` - Get lamp by ID
- `PUT /v1/lamps/{lampId}` - Update lamp
- `DELETE /v1/lamps/{lampId}` - Delete lamp (soft delete)
- `GET /health` - Health check endpoint

## Testing Strategy

### Test Coverage Requirements

Aim for >85% coverage across all implementations. Current metrics in `docs/COMPARISON.md`.

### Unit vs Integration Tests: Decision Heuristic

> **Ask: "Could this bug exist even if the database worked perfectly?"**
>
> - **Yes** → Unit test
> - **No** → Integration test

### What to Unit Test

Test the **"what"** of your business logic in isolation:

- **Validation logic** — Email format, required fields, business rules (e.g., "discount cannot exceed 50%")
- **Calculations and transformations** — Price computation, date manipulations, data mapping
- **State transitions** — Order status flows, user lifecycle states, workflow progressions
- **Conditional business logic** — "If user is premium, apply X"; "If order exceeds threshold, do Y"
- **Error handling paths** — Edge cases and malformed states that are hard to trigger via HTTP
- **Pure functions** — Any function that takes input and returns output without side effects

**Characteristics:**
- No database, no HTTP, no file system
- Fast execution (milliseconds)
- If you need to mock more than one or two dependencies, it's probably an integration test in disguise
- Test the logic, not the wiring

**Examples:**
```
✓ validateEmail("bad-email") returns validation error
✓ calculateOrderTotal(items, discount) returns correct sum
✓ canUserAccessResource(user, resource) returns true/false based on permissions
✓ parseImportFile(rawData) transforms to expected structure
```

### What to Integration Test

Test the **"how"** of your system components working together:

- **Happy path per endpoint** — POST creates resource, GET retrieves it, PUT updates, DELETE removes
- **Database constraints** — Unique constraints, foreign keys, cascades behave as expected
- **Query correctness** — Filters, pagination, sorting return expected results
- **Transaction behavior** — Rollback on failure, concurrent access handling
- **Authentication/authorization** — Correct HTTP status codes for authenticated/unauthorized requests
- **Serialization edge cases** — Nulls, empty arrays, dates, timezone handling in JSON responses
- **Error responses** — API returns proper error format and status codes for invalid requests

**Characteristics:**
- Real database (containerized, reset between runs)
- Real HTTP requests to your API
- Minimal test data setup—just enough to verify the scenario
- One test per meaningful scenario, not per code path
- Slower execution (acceptable: seconds per test)

**Examples:**
```
✓ POST /users with valid data returns 201 and creates user in database
✓ GET /users?status=active returns only active users
✓ POST /users with duplicate email returns 409 Conflict
✓ DELETE /orders/{id} cascades to order_items
✓ GET /protected-resource without token returns 401
```

### What NOT to Test

**Don't unit test:**
- Simple getters/setters or data classes with no logic
- Framework behavior (your ORM's save method works)
- Code paths already covered by integration tests with no additional business logic

**Don't integration test:**
- Every permutation of validation errors (unit test the validator instead)
- Internal implementation details
- Scenarios already covered by contract tests (e.g., Schemathesis)

### Test Placement Summary

| Scenario | Test Type |
|----------|-----------|
| Email validation rejects invalid format | Unit |
| POST /users rejects invalid email with 400 | Integration |
| Price calculation with discount | Unit |
| GET /orders returns correct total | Integration |
| Permission check logic | Unit |
| Unauthorized request returns 401 | Integration |
| Data transformation function | Unit |
| Database unique constraint enforced | Integration |
| State machine transitions | Unit |
| Concurrent updates don't corrupt data | Integration |

### Testing Guidelines for Contributors

When writing tests:

1. **Default to integration tests** for endpoint behavior—they catch real bugs
2. **Extract to unit tests** when you find yourself writing multiple integration tests to cover logic branches
3. **Never mock the database in integration tests**—use a real instance (Testcontainers)
4. **Keep unit tests focused**—one assertion per logical concept
5. **Name tests by behavior**, not by method: `rejects_order_when_inventory_insufficient` not `test_create_order_3`

### Running Integration Tests

```bash
# TypeScript (uses testcontainers)
cd src/typescript && npm run test:integration

# Python
cd src/python && poetry run pytest tests/integration/

# Go
cd src/go && go test ./... -tags=integration
```

## Code Quality Metrics

Metrics are tracked in `docs/COMPARISON.md`. To update metrics:

```bash
# Run from repository root
./.github/workflows/scripts/update-metrics.sh
```

Metrics include:
- Lines of code (total and excluding generated)
- Test coverage percentage
- Test/app code ratio
- Cyclomatic complexity (where tooling exists)

## CI/CD

### GitHub Actions Workflows

Each language has dedicated CI workflow:
- `typescript-ci.yml` - TypeScript build, test, lint
- `python-ci.yml` - Python build, test, lint
- `go-ci.yml` - Go build, test, lint
- `java-ci.yml` - Java build, test, lint
- `kotlin-ci.yml` - Kotlin build, test, lint
- `csharp-ci.yml` - C# build, test, lint
- `database-ci.yml` - Database schema validation

Workflows run on:
- Push to main/develop
- Pull requests
- Changes to respective `src/<language>/` directories

### Pre-commit Hooks

Session hooks in `.claude/hooks/` handle:
- Dependency installation for all languages
- Linter installation (golangci-lint, etc.)
- Environment setup for remote Claude Code sessions

## Contributing Guidelines

When adding features or fixing bugs:

1. **Cross-language consistency**: If changing API behavior, update all 6 implementations
2. **OpenAPI first**: Update `docs/api/openapi.yaml` before implementation
3. **Tests required**: Maintain or improve coverage (target >85%)
4. **Follow language idioms**: Use language-specific best practices, not one-size-fits-all
5. **Mode support**: Ensure migrate/serve-only/serve modes work correctly
6. **Documentation**: Update language README if adding dependencies or changing setup

### Before Creating a PR

Always run the linter/formatter for the language(s) you changed to ensure CI will pass:

```bash
# TypeScript
cd src/typescript && npm run lint && npm run format

# Python
cd src/python && poetry run black --check . && poetry run ruff check .

# Java
cd src/java && mvn spotless:check    # fix with: mvn spotless:apply

# Kotlin
cd src/kotlin && ./gradlew ktlintCheck  # fix with: ./gradlew ktlintFormat

# Go
cd src/go && make lint

# C#
cd src/csharp && dotnet format --verify-no-changes
```

### Branch Naming

Use pattern: `<language>-<feature>` (e.g., `typescript-graphql-support`, `go-mongodb-integration`)

### Commit Messages

Follow conventional commits:
- `feat(typescript): add GraphQL endpoint`
- `fix(go): handle null timestamps in queries`
- `docs: update Python setup instructions`
- `test(java): add integration tests for pagination`

## Troubleshooting

### Database Connection Issues

```bash
# Check if databases are running
docker-compose ps

# View logs
docker-compose logs postgres
docker-compose logs mysql
docker-compose logs mongodb

# Restart specific database
docker-compose restart postgres
```

### TypeScript: Prisma Issues

```bash
cd src/typescript
npx prisma generate  # Regenerate client
npx prisma migrate reset  # Reset database (CAUTION: destroys data)
```

### Go: sqlc Generation

```bash
cd src/go
sqlc generate  # Regenerate type-safe SQL code
```

### Java: Flyway Migration Conflicts

```bash
cd src/java
mvn flyway:clean  # CAUTION: destroys data
mvn flyway:migrate
```

### Port Conflicts

Default ports:
- TypeScript: 3000
- Python: 8000
- Go: 8080
- Java: 8081
- Kotlin: 8082
- C#: 8083
- PostgreSQL: 5432
- MySQL: 3306
- MongoDB: 27017

Override with environment variables or CLI flags (language-specific).

## Remote Development (Claude Code Web)

The `.claude/hooks/user-prompt-submit.sh` hook automatically:
- Downloads dependencies for all languages (on first session)
- Installs linters (golangci-lint, etc.)
- Sets up PATH environment variables

This runs only in remote Claude Code sessions (`CLAUDE_CODE_REMOTE=true`), not locally.
