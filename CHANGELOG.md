# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Features
### Bug Fixes

---

## [v1.1.1] — 2026-03-15

**Goal**: Productionize the Cloud Run deployments with Cloud SQL socket support, upgrade to Python 3.14, and deepen benchmark tooling with JIT warmup profiling.

### Features

- **benchmarks**: Add JIT warmup profiling suite (#429)
- **benchmarks**: Add `--services` filter to run a subset of languages (#405)
- **csharp**: Add Cloud Build deployment configuration (#432)
- **csharp**: Upgrade from .NET 8 to .NET 10 (#428)
- **python**: Use `RETURNING` for INSERT, UPDATE and DELETE (#426)
- **python**: Optimize FastAPI deployment with direct uvicorn entrypoint (#422)

### Bug Fixes

- **csharp**: Point buildpack path to solution root to pick up global.json (#435)
- **csharp**: Use google-24 builder instead of latest in Cloud Build (#434)
- **csharp**: Eliminate double DB round-trips by owning timestamps in application code (#418)
- **csharp**: Push pagination to database level via `ListAsync` (#406)
- **csharp**: Support Cloud SQL unix socket `DATABASE_URL` (#389)
- **java**: Use latest buildpacks builder to fix Docker API version mismatch (#438)
- **python**: Upgrade pydantic to 2.12 for Python 3.14 compatibility (#421)
- **python**: Commit read-only sessions to avoid spurious rollbacks (#408)
- **python**: Commit transaction after creating lamp (#407)
- **python**: Normalize `DATABASE_URL` timeout for asyncpg (#394)
- **kotlin**: Support Cloud SQL socket `DATABASE_URL` parsing (#392)
- **typescript**: Normalize Cloud SQL socket `DATABASE_URL` for Prisma (#390)
- **java**: Support Cloud SQL socket `DATABASE_URL` and Cloud Run refresh (#393)
- **benchmarks**: Use `DATABASE_URL` for csharp in services.json (#431)
- **benchmarks**: Replace `uuid_generate_v5` with `gen_random_uuid` in seed command (#404)
- Fix Npgsql connection pool and update configurations (#425)
- Fix missing `InetAddress` resolver (#399)
- Fix FastAPI traceback handling (#396)
- Ensure run-image stack matches builder in Cloud Build (#400)

### Changes

- Delegate timestamps to Postgres (Python, Go, Kotlin, C#) (#439)
- Refactor repository methods to return updated entities (#437)
- **kotlin**: Remove custom Hikari pool config, use defaults (#436)
- Update Python configuration to 3.14 (#419)
- Update Kotlin DB defaults and transaction isolation config (#414)
- Improve DB retry configuration and update logic (#415)
- Add pageable `findAllActive` query (#417)
- Adjust Kotlin and C# logging (#416)
- Refactor API and repository wiring around DB sessions (#409)
- Update PostgreSQL schema to v2 with optimized indexes (#395)
- Add Cloud SQL socket factory dependency (#397)
- Add Java run Dockerfile (#398)
- Use Secret Manager for `DATABASE_URL` in benchmarks (#403)
- Replace `unixSocketPath` with `ipTypes` for Cloud SQL TCP (#402)
- Add Cloud SQL instance property (#401)
- Remove custom Java run image (#420)
- Python/procfiles (#423)

### Documentation

- Refresh root README to match current project state (#388)
- Update agent guidance documentation (#410)

---

## [v1.1.0] — 2026-02-17

**Goal**: Consolidate the project to six core languages (TypeScript, Python, Java, Kotlin, Go, C#) by dropping PHP, then add persistent PostgreSQL storage to all six. The release also fixes cursor-based pagination to be genuinely database-backed, introduces three operation modes (`migrate` / `serve-only` / `serve`) for zero-downtime production deployments, enforces ≥80% test coverage across all implementations, and ships a Cloud Run benchmark harness for language performance comparison.

### Architecture Decision Records

#### Cross-language

| ADR                                                   | Title                      | Decision                                                                                                                                                                                                                                                                                                                     |
| ----------------------------------------------------- | -------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [ADR-005](docs/adr/005-postgresql-storage-support.md) | PostgreSQL Storage Support | Implement PostgreSQL storage with native connection pooling and migration tooling in all six language implementations. Each language uses its idiomatic ORM/driver: Prisma (TypeScript), SQLAlchemy 2.0/asyncpg (Python), Spring Data JPA/HikariCP (Java), Exposed/HikariCP (Kotlin), pgx v5/sqlc (Go), EF Core/Npgsql (C#). |

#### TypeScript

| ADR                                                                      | Title                          | Decision                                                                                                  |
| ------------------------------------------------------------------------ | ------------------------------ | --------------------------------------------------------------------------------------------------------- |
| [ADR-007](src/typescript/docs/adr/007-postgresql-storage.md) | PostgreSQL Storage with Prisma | Use Prisma 5.x as the ORM with auto-generated TypeScript types; run Prisma Migrate for schema management. |

#### Python

| ADR                                                                      | Title                                  | Decision                                                                        |
| ------------------------------------------------------------------------ | -------------------------------------- | ------------------------------------------------------------------------------- |
| [ADR-007](src/python/docs/adr/007-postgresql-storage.md) | PostgreSQL Storage with SQLAlchemy 2.0 | Use SQLAlchemy 2.0 async engine with asyncpg driver and Alembic for migrations. |

#### Java

| ADR                                                                    | Title                                   | Decision                                                                                                                |
| ---------------------------------------------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| [ADR-007](src/java/adr/007-postgresql-storage.md)               | PostgreSQL Storage with Spring Data JPA | Use Spring Data JPA with Hibernate and HikariCP connection pool; Flyway manages schema migrations.                      |
| [ADR-008](src/java/adr/008-java-test-tools-selection.md)        | Java Test Tools Selection               | Use JUnit 5, Mockito, Testcontainers, AssertJ, Awaitility, and JaCoCo for comprehensive testing and coverage reporting. |

#### Kotlin

| ADR                                                              | Title                               | Decision                                                                      |
| ---------------------------------------------------------------- | ----------------------------------- | ----------------------------------------------------------------------------- |
| [ADR-006](src/kotlin/adr/006-postgresql-storage.md) | PostgreSQL Storage with Exposed ORM | Use JetBrains Exposed ORM with HikariCP for idiomatic Kotlin database access. |

#### Go

| ADR                                                      | Title                          | Decision                                                                                   |
| -------------------------------------------------------- | ------------------------------ | ------------------------------------------------------------------------------------------ |
| [ADR-005](src/go/adr/005-postgresql-storage.md) | PostgreSQL Storage with pgx v5 | Use pgx v5 with pgxpool for connection pooling and sqlc for type-safe SQL code generation. |

#### C#

| ADR                                                              | Title                           | Decision                                                                                           |
| ---------------------------------------------------------------- | ------------------------------- | -------------------------------------------------------------------------------------------------- |
| [ADR-007](src/csharp/adr/007-postgresql-storage.md) | PostgreSQL Storage with EF Core | Use Entity Framework Core 8.0+ with the Npgsql provider; EF Core Migrations manage schema changes. |

### Features

- **go**: Implement effective pagination for `GET /v1/lamps` (#378)
- **kotlin**: Implement bounded pagination for `GET /v1/lamps` (#381)
- **java**: Improve Java 21 idiomatics (#358)
- **java**: Add Lombok to reduce boilerplate code (#348)
- **java**: Support postgres URI forms for `DATABASE_URL` (#369)
- **csharp**: Support `DATABASE_URL` fallback for connection string (#367)
- **devx**: Add pre-commit hooks and make CI lint checks non-mutating (#383)
- Add single-instance Cloud Run benchmark harness (#365)

### Bug Fixes

- **python**: Implement effective pagination for `GET /v1/lamps` (#379)
- **python**: Exclude generated Pydantic models from coverage measurement (#342)
- **python**: Exclude test files from coverage measurement (#339)
- **python**: Exclude entry point files from coverage measurement (#338)
- **python**: Generate coverage JSON readable by `extract-coverage.sh` (#323)
- **python**: Enable mode-testing in CI and fix CLI invocation (#325)
- **java**: Use line coverage instead of instruction coverage for JaCoCo threshold (#341)
- **java**: Exclude entry point and config classes from JaCoCo coverage (#337)
- **java**: Enforce JaCoCo coverage check and add tests to meet 80% threshold (#328)
- **csharp**: Track Cobertura coverage report in git for CI metrics (#326)
- **csharp**: Generate Cobertura coverage compatible with `extract-coverage.sh` (#322)
- **kotlin**: Track JaCoCo coverage report in git for CI metrics (#324)
- **go**: Fix pre-commit: remove sql hook and fix go package iteration (#386, #387)
- Fix Python mode testing race condition with table check retry (#286)
- Fix Kotlin Cloud Run deployment by adding Shadow plugin for fat JAR (#283)
- Fix Python Cloud Run deployment by syncing requirements.txt (#284)
- Fix Java mode testing by fixing conditional bean configuration (#279)
- Fix C# mode testing by adding `ConnectionStrings__LampControl` env var (#278)
- Fix Python mode testing by adding CLI port argument support (#277)
- Fix mode testing for Python, Java, C#, Kotlin, and TypeScript (#271)

### Refactoring

- **go**: Use `sync.Map` and page-only copying in in-memory list (#385)
- **go**: Improve idiomatic Go patterns (#361)
- **go**: Use Makefile coverage-check target in CI (#363)
- **kotlin**: Improve idiomatic Kotlin patterns (#362)
- **kotlin**: Optimize test suite by removing redundant tests (#315)
- **csharp**: Improve C# implementation idiomatics (#359)
- **typescript**: Improve idiomatics (#356)
- **python**: Improve idiomatics across the implementation (#357)
- Refactor imports and update detekt config (#276)
- Optimize Kotlin imports by allowing wildcard imports (#310)

### Tests

- **python**: Add tests to reach 80% coverage threshold (#329)
- **python**: Increase test coverage to 82% (#317)
- **typescript**: Fix misleading 100% coverage reporting (#319)
- Add comprehensive unit tests for PrismaLampRepository (#316)

### CI

- **python**: Enforce 80% minimum code coverage threshold (#327)
- **csharp**: Enforce 80% minimum code coverage threshold (#340)
- Add release-\* branches to CI workflow triggers (#343)

### Documentation

- **go**: Clarify `DATABASE_URL` accepts `postgres` and `postgresql` schemes (#368)
- **java**: Add testing and code coverage documentation (#344)
- Add CLAUDE.md for Claude Code development guidance (#314)
- Add PostgreSQL implementation analysis documentation (#287)

### Infrastructure

- Change default mode from `serve` to `serve-only` (#268)
- Add three operation modes to all implementations: `migrate`, `serve-only`, `serve` (#264)
- Remove `USE_POSTGRES` flag requirement from TypeScript (#280)
- Remove PHP and Ruby implementations (#260)
- Add PostgreSQL support with Prisma and integration tests — TypeScript (#259)
- Add PostgreSQL backend and Alembic migrations — Python (#258)
- Add PostgreSQL support with Exposed ORM — Kotlin (#255)
- Add PostgreSQL storage support with Spring Data JPA — Java (#253)
- Add PostgreSQL storage with conditional activation — Go (#249)
- Implement PostgreSQL storage with Entity Framework Core — C# (#250)
- Add Flyway database migrations — Kotlin (#261)
- Go migration support (#263)
- Add SessionStart hook for Claude Code linter configuration (#265)
- Add Gradle wrapper support and Kotlin setup to session hook (#312)
- Remove `USE_POSTGRES` env var references from CI and docs (#366)
- Add postgres support ADRs (#247)
- Add release process documentation (#246)

---

## [v1.0.0] — 2025-12-31

**Goal**: Establish a reference implementation of a simple REST API across seven languages — TypeScript, Python, Java, Kotlin, Go, C#, and PHP — all driven by a single OpenAPI specification. The release focuses on API correctness (Schemathesis contract testing on every CI run, all implementations backed by in-memory storage), observability (health endpoints, structured logging, code metrics), and a fair comparison baseline (identical API surface, Docker images, ≥80% test coverage). Ruby was planned but reached only the ADR stage.

### Architecture Decision Records

#### Cross-language

| ADR                                                 | Title                                   | Decision                                                                                                                                                                                   |
| --------------------------------------------------- | --------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [ADR-001](docs/adr/001-lamp-status-boolean.md)      | Lamp Status as Boolean                  | Represent lamp on/off state as a boolean field `is_on` (not an ENUM) for storage efficiency and cross-language type safety.                                                                |
| [ADR-002](docs/adr/002-pagination-strategy.md)      | Cursor-Based Pagination                 | Use opaque cursor tokens with `cursor`, `pageSize`, `nextCursor`, and `hasMore` for all list endpoints — avoids skip/offset inconsistencies on large datasets.                             |
| [ADR-003](docs/adr/003-offline-first-api.md)        | Offline-First API Design                | Adopt ETag-based conditional requests and cache-control headers to support intermittent connectivity scenarios.                                                                            |
| [ADR-004](docs/adr/004-schemathesis-integration.md) | Schemathesis for Property-Based Testing | Use Schemathesis in every language's CI pipeline to automatically generate and run 100 test cases per OpenAPI operation, ensuring all implementations conform to the shared specification. |

#### TypeScript

| ADR                                                                           | Title                    | Decision                                                                                                                     |
| ----------------------------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| [ADR-001](src/typescript/docs/adr/001-project-setup.md)                       | Project Setup            | TypeScript with clean domain/application/infrastructure layered architecture.                                                |
| [ADR-002](src/typescript/docs/adr/002-nodejs-and-npm-versions.md)             | Node.js & npm Versions   | Node.js 22.x (LTS until April 2027) and npm 10.x as minimum requirements.                                                    |
| [ADR-003](src/typescript/docs/adr/003-typescript-version.md)                  | TypeScript Version       | TypeScript 5.x (latest stable).                                                                                              |
| [ADR-004](src/typescript/docs/adr/004-typescript-http-framework.md)           | HTTP Framework           | Fastify — chosen for performance, built-in validation, and type safety.                                                      |
| [ADR-005](src/typescript/docs/adr/005-npm-package-manager-selection.md)       | Package Manager          | npm — default with Node.js, universal CI/CD compatibility.                                                                   |
| [ADR-006](src/typescript/docs/adr/006-schemathesis-integration.md)            | Schemathesis Integration | Property-based API testing via Schemathesis on `http://localhost:3000/v1`.                                                   |
| [ADR-007](src/typescript/docs/adr/007-nodejs-type-stripping-compatibility.md) | Node.js Type Stripping   | Enable native TypeScript execution via Node.js type stripping — updated tsconfig, explicit `type` imports, `.ts` extensions. |

#### Python

| ADR                                                                | Title                    | Decision                                                                                                                  |
| ------------------------------------------------------------------ | ------------------------ | ------------------------------------------------------------------------------------------------------------------------- |
| [ADR-001](src/python/docs/adr/001-project-setup.md)                | Project Setup            | Python with clean architecture; Poetry, pytest, pylint/flake8, mypy.                                                      |
| [ADR-002](src/python/docs/adr/002-python-version.md)               | Python Version           | Python 3.12 (minimum 3.12.9) — enhanced error messages, ~5% performance improvement, security support until October 2028. |
| [ADR-003](src/python/docs/adr/003-python-dependency-management.md) | Dependency Management    | Poetry — deterministic builds, `pyproject.toml` standard, intuitive CLI.                                                  |
| [ADR-004](src/python/docs/adr/004-schemathesis-integration.md)     | Schemathesis Integration | Property-based API testing on `http://localhost:8000/v1`.                                                                 |
| [ADR-005](src/python/docs/adr/005-fastapi-framework-selection.md)  | HTTP Framework           | FastAPI — automatic OpenAPI generation, async support, high performance, type safety.                                     |

#### Java

| ADR                                                          | Title                    | Decision                                                                                                    |
| ------------------------------------------------------------ | ------------------------ | ----------------------------------------------------------------------------------------------------------- |
| [ADR-001](src/java/adr/001-adopt-java-21-lts.md)             | Java Version             | Java 21 (LTS) — released September 2023, supported until at least September 2031.                           |
| [ADR-002](src/java/adr/002-select-eclipse-temurin-21.md)     | Java Runtime             | Eclipse Temurin 21 — TCK-certified, open-source, GPLv2.                                                     |
| [ADR-003](src/java/adr/003-maven-build-tool-selection.md)    | Build Tool               | Apache Maven — industry standard, convention-over-configuration, first-class Spring Boot integration.       |
| [ADR-004](src/java/adr/004-select-spring-boot-openapi.md)    | Framework & OpenAPI      | Spring Boot 3.x with springdoc-openapi and OpenAPI Generator — contract-first development.                  |
| [ADR-005](src/java/adr/005-async-request-handling.md)        | Async Strategy           | Spring MVC with `@Async` and `CompletableFuture` for I/O operations (not reactive WebFlux).                 |
| [ADR-006](src/java/adr/006-imperative-vs-reactive-stack.md)  | Reactive vs Imperative   | Spring MVC imperative stack — simpler mental model, easier debugging, sufficient for this API's throughput. |
| [ADR-007](src/java/adr/007-java-linter-and-formatter.md)     | Linter & Formatter       | Spotless + Google Java Format for formatting; SpotBugs, PMD, Checkstyle for static analysis.                |
| [ADR-009](src/java/adr/009-schemathesis-integration.md)      | Schemathesis Integration | Property-based API testing on `http://localhost:8081/v1`.                                                   |

#### Kotlin

| ADR                                                | Title              | Decision                                                                           |
| -------------------------------------------------- | ------------------ | ---------------------------------------------------------------------------------- |
| [ADR-001](src/kotlin/adr/001-kotlin-version.md)    | Kotlin Version     | Kotlin 2.1.21 (latest stable) — no LTS policy, always track latest.                |
| [ADR-002](src/kotlin/adr/002-kotlin-runtime.md)    | Runtime            | JVM runtime (not Kotlin/Native or Kotlin/JS).                                      |
| [ADR-003](src/kotlin/adr/003-gradle-build-tool-selection.md) | Build Tool         | Gradle with Kotlin DSL — idiomatic, flexible, strong Kotlin/JVM ecosystem support. |
| [ADR-004](src/kotlin/adr/004-web-framework-selection.md)     | HTTP Framework     | Ktor — JetBrains-maintained, coroutine-native, lightweight.                        |
| [ADR-005](src/kotlin/adr/005-openapi-code-generation.md)     | OpenAPI Generation | OpenAPI Generator with `kotlin-server` template for server stub generation.        |

#### Go

| ADR                                                   | Title                    | Decision                                                                         |
| ----------------------------------------------------- | ------------------------ | -------------------------------------------------------------------------------- |
| [ADR-001](src/go/adr/001-Go-Version.md)                                  | Go Version               | Go 1.24.3 — latest stable with security and performance updates.                 |
| [ADR-002](src/go/adr/002-HTTP-Framework.md)                              | HTTP Framework           | Chi — idiomatic Go, lightweight, `net/http`-compatible, minimal dependencies.    |
| [ADR-003](src/go/adr/003-go-modules-dependency-management.md)            | Dependency Management    | Go modules (`go.mod`) — standard Go toolchain, reproducible builds.              |
| [ADR-004](src/go/adr/004-use-oapi-codegen-for-server-generation.md)      | OpenAPI Code Generation  | `oapi-codegen` — generates idiomatic Go server interfaces from the OpenAPI spec. |
| [ADR-007](src/go/adr/007-schemathesis-integration.md) | Schemathesis Integration | Property-based API testing on `http://localhost:8080/v1`.                        |

#### C#

| ADR                                                       | Title                    | Decision                                                                         |
| --------------------------------------------------------- | ------------------------ | -------------------------------------------------------------------------------- |
| [ADR-001](src/csharp/adr/001-csharp-version.md)           | Language Version         | C# 12.0 with .NET 8 (LTS, supported until November 2026).                        |
| [ADR-002](src/csharp/adr/002-framework.md)                | HTTP Framework           | ASP.NET Core with minimal APIs.                                                  |
| [ADR-003](src/csharp/adr/003-dotnet-build-tool-selection.md) | Build Tool               | `dotnet` CLI — standard .NET toolchain, excellent CI/CD integration.             |
| [ADR-004](src/csharp/adr/004-openapi-generation.md)       | OpenAPI Generation       | NSwag or Swashbuckle for server stub and client generation from the shared spec. |
| [ADR-005](src/csharp/adr/005-schemathesis-integration.md) | Schemathesis Integration | Property-based API testing on `http://localhost:8083/v1`.                        |
| [ADR-006](src/csharp/adr/006-package-management.md)       | Package Management       | NuGet via `dotnet` CLI — default .NET package manager.                           |

### API

- OpenAPI 3.0 specification as source of truth (`docs/api/openapi.yaml`)
- `GET /v1/lamps` — list lamps with cursor-based pagination
- `POST /v1/lamps` — create lamp
- `GET /v1/lamps/{lampId}` — get lamp by ID
- `PUT /v1/lamps/{lampId}` — update lamp
- `DELETE /v1/lamps/{lampId}` — soft delete lamp
- `GET /health` — health check
- 400 `INVALID_ARGUMENT` error responses (#122)
- All implementations updated to match spec with timestamps and pagination (#124, #131, #135, #141, #149, #151, #156)

### Features

- Seven language implementations: TypeScript, Python, Java, Kotlin, Go, C#, PHP (Ruby planned, ADRs only)
- Schemathesis contract testing in every CI pipeline (#117, #126, #137, #145, #158, #160, #168)
- `/health` endpoint across all implementations (#172, #174, #176, #178, #180, #182, #184)
- Multi-stage Docker images for Go (#216) and Java (#186)
- Structured logging with correlation IDs (#51)
- Code coverage dashboards and metrics (`docs/COMPARISON.md`)
- GraphQL API with Apollo Server (#31)
- gRPC implementation (#33)
- TypeScript MongoDB implementation (#32)
- SQL database repositories (#34)

### Bug Fixes

- Fix Java API 500 error: set proper timestamps in Lamp constructor (#147)
- Fix Java API validation errors returning HTTP 500 instead of HTTP 400 (#143, #139)
- Fix Python API error response format to match OpenAPI spec (#166)
- Fix Python API to return 400 instead of 500 for null request bodies (#164)
- Fix Python API validation errors and datetime format compliance (#162)
- Fix Cloud Run deployment failures for Python (#225) and Kotlin (#283)
- Fix PHP code coverage reporting and 80% threshold enforcement (#212)
- Fix GitHub Actions deprecated actions (#200)
- Fix SCC generated file detection in metrics workflow (#134)

### CI/CD

- TypeScript CI with linting and testing (#29)
- Java CI with PMD static analysis and JaCoCo coverage (#78, #82)
- Go code coverage reporting (#105)
- Kotlin CI with ktlint and detekt (#110)
- C# CI with Cobertura coverage (#94, #113)
- SQL linting with reviewdog (#20)
- GitHub Copilot custom instructions for all implementations (#108)
- Poetry caching in Python CI (#202)
- Modernized Kotlin CI with `gradle/actions/setup-gradle@v4` (#203)
- devcontainer configuration and Dependabot setup (#73)

### Documentation

- Comprehensive README for open-source project comparison (#153)
- Apache License 2.0 (#154)
- Implementation comparison (`docs/COMPARISON.md`) with SLOC, ULOC, dryness, and coverage metrics
- ADRs for all language and framework selections
- ULOC metrics table (#204)
- GitHub Copilot setup workflow (#209)

---

[Unreleased]: https://github.com/davideme/lamp-control-api-reference/compare/v1.1.1...HEAD
[v1.1.1]: https://github.com/davideme/lamp-control-api-reference/compare/v1.1.0...v1.1.1
[v1.1.0]: https://github.com/davideme/lamp-control-api-reference/compare/v1.0.0...v1.1.0
[v1.0.0]: https://github.com/davideme/lamp-control-api-reference/releases/tag/v1.0.0
