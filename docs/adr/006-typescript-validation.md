# ADR 006: TypeScript Validation Library

**Status:** Proposed

**Date:** 2025-04-18

## Context

Robust validation of incoming data (API requests, configuration, etc.) and internal data structures is crucial for application correctness and security. We need a validation library that integrates well with TypeScript, providing static type inference from schemas and clear runtime validation errors.

## Decision

We will use **Zod** as the primary validation library for the TypeScript implementation.

-   _Rationale:_
    -   **Type Safety First:** Zod schemas inherently define TypeScript types, eliminating the need for separate type definitions and ensuring schemas and types stay synchronized.
    -   **Simplicity and Expressiveness:** Offers a fluent, chainable API for defining complex schemas easily.
    -   **Framework Agnostic:** Can be used to validate any data structure, not just HTTP request bodies.
    -   **Excellent TypeScript Integration:** Designed specifically for TypeScript, providing superior static analysis and developer experience.
    -   **Runtime Safety:** Performs thorough runtime checks based on the defined schemas.

## Consequences

-   **Pros:**
    -   Greatly enhances type safety and reduces runtime errors by catching invalid data early.
    -   Improves developer experience through static type inference and clear validation logic.
    -   Single source of truth for data shapes and validation rules.
    -   Active development and strong community support.
-   **Cons:**
    -   Adds a dependency to the project.
    -   Developers need to learn the Zod schema definition syntax.

## Alternatives Considered

-   **Joi:** A mature and popular validation library, originally from the Hapi framework. Less integrated with TypeScript type inference compared to Zod.
-   **`class-validator`:** Uses decorators on classes for validation. Ties validation logic closely to class definitions, which can be less flexible for validating plain objects or function arguments. Requires enabling experimental decorators.
-   **Manual Validation:** Writing custom validation logic for every case. This is highly error-prone, verbose, and difficult to maintain.
-   **TypeORM Validation:** Relies on decorators and is tied to TypeORM entities.
-   **Prisma Client Extensions (for validation):** Possible but less ergonomic and general-purpose than a dedicated validation library like Zod.

## References

-   [Zod Documentation](https://zod.dev/)
-   [Joi](https://joi.dev/)
-   [`class-validator`](https://github.com/typestack/class-validator) 