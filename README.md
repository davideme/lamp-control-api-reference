# Lamp Control API Reference

[![Database CI](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-ci.yml/badge.svg)](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-ci.yml)

Reference implementations of the same Lamp CRUD API in multiple languages, designed for side-by-side comparison of architecture, tooling, testing, and developer experience.

## Current Status

- 6 language implementations: TypeScript, Python, Java, C#, Go, Kotlin
- REST API implemented across all languages from a shared OpenAPI contract
- Three runtime modes supported (`migrate`, `serve-only`, `serve`) for CI/CD and production workflows
- In-memory and PostgreSQL-backed execution paths are available in the implementations
- GraphQL and gRPC contract files are present in `docs/api/`, but runtime implementations are still REST-first

## Repository Layout

```text
lamp-control-api-reference/
├── src/                    # Language implementations
│   ├── typescript/         # Fastify
│   ├── python/             # FastAPI
│   ├── java/               # Spring Boot
│   ├── csharp/             # ASP.NET Core
│   ├── go/                 # Chi + oapi-codegen + sqlc
│   └── kotlin/             # Ktor
├── docs/                   # Project docs, ADRs, and API contracts
│   └── api/
│       ├── openapi.yaml
│       ├── graphql.graphql
│       └── lamp.proto
├── database/               # SQL and MongoDB schema/setup assets
├── scripts/                # CI/local helper scripts (including mode tests)
└── .github/workflows/      # Per-language CI and metrics workflows
```

## Quick Start

1. Pick an implementation in `src/` and open its README.
2. Start infrastructure if needed:
   ```bash
   docker-compose up -d postgres
   ```
3. Run the selected implementation with its local instructions.
4. Verify the API with:
   - `GET /health`
   - `GET /v1/lamps`

For cross-language mode testing, see `scripts/README.md` and `scripts/test-modes-local.sh`.

## Key Documentation

- `docs/COMPARISON.md`: generated metrics and coverage snapshot
- `docs/OPERATION_MODES.md`: mode semantics and deployment patterns
- `docs/api/openapi.yaml`: REST contract
- `docs/api/graphql.graphql`: GraphQL schema contract
- `docs/api/lamp.proto`: gRPC contract
- `CONTRIBUTING.md`: contribution and workflow guidelines

## CI and Quality

Each language has a dedicated CI workflow under `.github/workflows/` that validates build/test quality. The repository also includes:

- database schema validation workflow
- SQL linting
- Schemathesis API contract tests
- automated metrics updates

## License

This project is licensed under Apache License 2.0. See `LICENSE`.
