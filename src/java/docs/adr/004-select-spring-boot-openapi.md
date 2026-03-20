# ADR 004: Select Spring Boot 3.x with OpenAPI Generator for Contract-First API Implementation

**Status:** Proposed
**Date:** 2025-05-21

**Context:**
The project will expose a REST API defined using an OpenAPI 3.1 specification. To ensure consistency, maintainability, and automation, the backend implementation should be driven by the OpenAPI contract. The framework must support contract-first development, align with Java 21, and be compatible with long-term maintainability and team expertise.

**Decision:**
The project will use **Spring Boot 3.x with springdoc-openapi and OpenAPI Generator** for API implementation.

**Consequences:**

* The OpenAPI spec will serve as the source of truth, enabling client/server generation, API documentation, and validation.
* Spring Boot 3.x offers native support for Jakarta EE, Java 21, AOT compilation (including GraalVM native image), and wide ecosystem adoption.
* Code generation via **OpenAPI Generator** (Java Spring template) will create controller interfaces and models from the spec, reducing boilerplate.
* The **springdoc-openapi** library will generate and expose up-to-date Swagger documentation during development.

**Alternatives Considered:**

* **Quarkus with SmallRye OpenAPI:**

  * Fast startup and native image support.
  * More opinionated, less mature ecosystem for enterprise use compared to Spring.
  * Better suited for microservices with lightweight dependencies.
* **Micronaut:**

  * AOT-optimized, low memory usage, fast startup.
  * Good OpenAPI support via annotations or code-first generation.
  * Smaller ecosystem and less familiarity within most Java teams.
* **Javalin / Ktor (JVM microframeworks):**

  * Lightweight and flexible, good for minimal APIs or serverless use cases.
  * Lack deeper OpenAPI contract-first tooling and enterprise feature support.

**Rationale:**

* Spring Boot is an industry-standard with robust support for OpenAPI-driven workflows.
* It provides excellent integration with modern tooling (Maven, Gradle, Docker, Kubernetes), observability stacks, and security frameworks.
* The combination of OpenAPI Generator and Spring Boot enables clean separation between specification and implementation, easing long-term maintenance.
* The team already has experience with Spring, reducing onboarding time.

**References:**

* [Spring Boot 3.x Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [OpenAPI Generator](https://openapi-generator.tech/)
* [springdoc-openapi](https://springdoc.org/)
* [Quarkus OpenAPI Support](https://quarkus.io/guides/openapi-swaggerui)
* [Micronaut OpenAPI Module](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/)
