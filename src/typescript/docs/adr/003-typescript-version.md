# ADR 003: TypeScript Version

**Status:** Proposed

**Date:** 2025-04-18

## Context

The TypeScript language version is a fundamental choice for the project, impacting available features, syntax, and tooling compatibility. We need to select a specific version line to ensure consistency and leverage modern language capabilities. This decision builds upon [ADR-002](./002-nodejs-and-npm-versions.md) which defined the Node.js runtime environment.

## Decision

We will use **TypeScript 5.x** (the latest major stable version line at the time of writing) for the reference implementation.

-   _Rationale:_
    -   Provides the latest stable language features, enhancing developer productivity and code expressiveness.
    -   Offers improved type system capabilities compared to older versions.
    -   Ensures compatibility with the latest versions of libraries and frameworks in the Node.js ecosystem.
    -   Aligns with the goal of providing a modern reference implementation.

## Consequences

-   **Pros:**
    -   Access to the most up-to-date TypeScript features and performance improvements.
    -   Better integration with modern tooling and libraries.
    -   Improved developer experience through enhanced type checking and language services.
-   **Cons:**
    -   Slightly newer features might be less familiar to developers coming from older TypeScript versions.
    -   Requires tooling (like `tsc`, linters, IDEs) that supports TypeScript 5.x.

## Alternatives Considered

-   **TypeScript 4.x:** A widely used and stable version line, but lacks some newer features and improvements found in 5.x. Would be less "modern".
-   **Using only JavaScript (ESNext):** Forgoes the benefits of static typing provided by TypeScript, increasing the risk of runtime errors and reducing maintainability for a project of this scale.

## References

-   [TypeScript Official Website](https://www.typescriptlang.org/)
-   [TypeScript 5.0 Announcement](https://devblogs.microsoft.com/typescript/announcing-typescript-5-0/) (and subsequent 5.x releases) 