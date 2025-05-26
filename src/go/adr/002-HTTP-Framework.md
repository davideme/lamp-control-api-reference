# ADR 002: Selection of HTTP Routing Framework for Go REST API

**Status**: Accepted
**Date**: 2025-05-26
**Author**: \[Your Name]
**Context**: Our project requires a reliable, maintainable, and idiomatic HTTP routing solution for building a RESTful API in Go. The decision must align with our team's expertise, project requirements, and long-term maintainability goals.([dotnettricks.com][1], [Reddit][2])

## Considered Options

1. **Chi**

   * *Pros*:

     * Highly idiomatic, leveraging Go's `net/http` interfaces.
     * Lightweight with minimal dependencies.
     * Supports middleware chaining and route grouping.
     * Active community and well-maintained.
   * *Cons*:

     * Requires manual setup for features like input validation and OpenAPI documentation.

2. **Gin**

   * *Pros*:

     * High performance with built-in features like JSON validation and middleware support.
     * Large community and extensive documentation.
   * *Cons*:

     * Uses a custom `gin.Context`, which deviates from Go's standard `net/http` interfaces.
     * Less idiomatic, potentially leading to tighter coupling with the framework.

3. **Gorilla Mux**

   * *Pros*:

     * Rich routing capabilities, including regex support.
     * Widely used and battle-tested.
   * *Cons*:

     * No longer actively maintained.
     * Larger memory footprint compared to alternatives.

4. **Standard Library (`net/http`)**

   * *Pros*:

     * Fully idiomatic and part of Go's core.
     * No external dependencies.
   * *Cons*:

     * Requires significant boilerplate for routing, middleware, and other common functionalities.
     * Lacks features like route grouping and middleware chaining out-of-the-box.

## Decision

We have chosen to use **Chi** as our HTTP routing framework.([Till it's done][3])

## Rationale

Chi offers a balance between idiomatic Go design and the flexibility needed for building robust REST APIs. Its lightweight nature and adherence to `net/http` interfaces ensure that our codebase remains clean, maintainable, and aligned with Go best practices. While it requires manual integration for certain features, this promotes a better understanding of the components and avoids unnecessary abstractions.

## Consequences

* **Positive**:

  * Improved code maintainability due to idiomatic design.
  * Flexibility to integrate only necessary components, keeping the application lightweight.
  * Ease of onboarding for developers familiar with Go's standard library.

* **Negative**:

  * Additional effort required to implement features like input validation and API documentation.
  * Potential need to develop or integrate third-party middleware for common functionalities.

## References

* Chi GitHub Repository: [https://github.com/go-chi/chi](https://github.com/go-chi/chi)
* Comparison of Go Web Frameworks: [https://medium.com/@hasanshahjahan/a-deep-dive-into-gin-chi-and-mux-in-go-33b9ad861e1b](https://medium.com/@hasanshahjahan/a-deep-dive-into-gin-chi-and-mux-in-go-33b9ad861e1b)([Medium][4])
