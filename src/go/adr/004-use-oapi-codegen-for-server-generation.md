# ADR-004: Use `oapi-codegen` to generate Go server code from OpenAPI specification

## Status

Accepted

## Date

2025-06-17

## Context

We need to generate and maintain a Go server implementation based on an OpenAPI 3.0 specification for the LAMP Control API. The objectives are:

* Type-safe request/response handling
* Easy integration with the `chi` router
* Automatic request validation
* Maintainability and community support
* Consistent with the existing project structure

Several tools were evaluated for generating Go code from OpenAPI specifications:

| Tool              | Supports Chi                  | Type-Safe | Maintained | Notes                                        |
| ----------------- | ----------------------------- | --------- | ---------- | -------------------------------------------- |
| oapi-codegen      | ✅                             | ✅         | ✅          | Actively maintained, flexible output options |
| openapi-generator | ❌ (uses `gin`, `mux`, custom) | ✅         | ✅          | Heavy and harder to customize for chi        |
| go-swagger        | ❌ (uses `go-openapi`)         | ✅         | 🟡         | Large and complex; opinionated tooling       |

The `chi` router is preferred for its lightweight nature and idiomatic Go design patterns, making it a good fit for this microservice architecture.

## Decision

We will use [`oapi-codegen`](https://github.com/deepmap/oapi-codegen) to generate:

* Go types for OpenAPI schemas (`-generate types`)
* `chi`-compatible server interfaces (`-generate chi-server`)
* Request validation middleware (via OpenAPI spec)

This approach offers:

* Strong alignment with our existing use of `chi`
* Minimal boilerplate
* Easier testing and mocking via generated interfaces
* Clean separation of transport layer concerns
* Type safety across the entire request/response pipeline

Example usage:

```bash
oapi-codegen -generate types,chi-server -o api.gen.go -package api openapi.yaml
```

The generated code will be integrated into the existing Go project structure:

```
src/go/
├── adr/
├── internal/
│   ├── api/          # Generated API code
│   ├── handlers/     # Implementation of generated interfaces
│   └── models/       # Business logic models
├── cmd/
│   └── server/       # Main application entry point
└── pkg/              # Shared packages
```

## Consequences

### Positive

* **Type Safety**: All request/response types are generated and type-checked at compile time
* **Consistency**: Generated code ensures API implementation matches the OpenAPI specification
* **Maintainability**: Changes to the API require updating the OpenAPI spec, keeping documentation in sync
* **Testing**: Generated interfaces make unit testing easier through mocking
* **Performance**: `chi` router provides excellent performance characteristics

### Negative

* **Tool Dependency**: Ties us to OpenAPI 3.0 (not 3.1+ features)
* **Customization Limitations**: Customization requires post-processing or template overrides
* **Implementation Responsibility**: We are responsible for implementing all handler methods defined in the generated interfaces
* **Build Complexity**: Code generation must be integrated into the build process

### Mitigation Strategies

* High tool maturity and adoption reduces risk
* Clear boundaries in generated code make customization predictable
* Comprehensive testing of generated interfaces ensures reliability
* Documentation of code generation process in README

## Implementation Notes

1. The `oapi-codegen` tool will be integrated into the build process via Makefile
2. Generated code will be excluded from version control (added to `.gitignore`)
3. A configuration file will be used to standardize generation options
4. Handler implementations will be kept separate from generated code to avoid conflicts during regeneration

## References

* [oapi-codegen GitHub Repository](https://github.com/deepmap/oapi-codegen)
* [Chi Router Documentation](https://github.com/go-chi/chi)
* [OpenAPI 3.0 Specification](https://swagger.io/specification/)
