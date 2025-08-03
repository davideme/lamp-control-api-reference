---
applyTo: "src/go/**/*"
---

This Go implementation of the lamp control API uses modern Go patterns and in-memory storage.

Key frameworks and tools:
- Chi Router for lightweight HTTP routing
- oapi-codegen for OpenAPI code generation
- Standard library testing with comprehensive test coverage (80% minimum)
- golangci-lint for comprehensive linting
- In-memory map storage with sync.RWMutex for thread-safety

When working on this codebase:
- Follow Go 1.24.3+ features and idioms
- Maintain the thread-safe map storage pattern using sync.RWMutex
- Use the generated OpenAPI types and handlers from oapi-codegen
- Follow the existing Chi router patterns for endpoint handling
- Write comprehensive unit tests maintaining 80%+ code coverage
- Use the established error handling patterns with proper HTTP status codes
- Follow Go naming conventions and package organization
- Use context.Context for request handling where appropriate
- Maintain consistency with the lamp domain model (ID as string, Status as boolean)
- Use the Makefile targets for development workflow (build, test, lint, format)
- Exclude generated files (.gen.go) from manual modifications