# ADR 002: Select PHP Framework and Version for REST API

## Status

Accepted

## Date

2025-06-08

## Context

We are initiating a new PHP project with the primary objective of building a robust, maintainable, and scalable REST API. The framework choice should enable rapid development, ensure security, and provide strong community and ecosystem support over the coming years. **Requirement:** The framework must be backend-only without frontend dependencies or Node.js requirements.

## Decision

We will use **Slim Framework 4** as the framework for our REST API.

* **Version:** Slim Framework 4.x (latest stable)
* **PHP Compatibility:** PHP 8.1, 8.2, 8.3, and 8.4
* **Architecture:** Microframework designed specifically for APIs and web services

## Rationale

* **Backend-Only Design:** Slim is designed exclusively for backend development with no frontend dependencies or Node.js requirements.
* **Lightweight and Fast:** Minimal overhead with excellent performance for API endpoints and microservices.
* **PSR Standards Compliance:** Built on PSR-7 (HTTP Message Interface) and PSR-15 (HTTP Server Request Handlers), ensuring interoperability and modern PHP practices.
* **Mature and Stable:** Slim 4 is well-established with proven reliability in production environments.
* **Flexible Architecture:** Allows for clean separation of concerns with dependency injection container support.
* **API-First Approach:** Designed specifically for building REST APIs, web services, and microservices.
* **Minimal Learning Curve:** Simple, straightforward framework that's easy to learn and implement for API development.

## Alternatives Considered

* **Laravel 12:** Full-stack framework with frontend capabilities (Vue, React starters) and Node.js dependencies, which doesn't align with our backend-only requirement.
* **Symfony:** Highly modular and robust, ideal for complex, enterprise APIs, but has a steeper learning curve and requires more boilerplate.
* **API Platform:** Built on Symfony, specialized for API-first projects (REST & GraphQL), but introduces additional complexity and includes frontend tooling.
* **Lumen:** Laravel's microframework, but officially deprecated as of Laravel 10+ and no longer recommended for new projects.

## Risks

* **Feature Limitations:** Being a microframework, some advanced features may require additional packages or manual implementation.
* **Smaller Ecosystem:** Fewer pre-built packages compared to Laravel, requiring more custom development.
* **Less Opinionated:** Requires more architectural decisions from the development team.

## Implementation Notes

* Use Composer for dependency management
* Implement PSR-4 autoloading
* Utilize middleware for cross-cutting concerns (authentication, CORS, logging)
* Integrate with existing PHP ecosystem packages as needed

## References

* [Slim Framework Documentation](https://www.slimframework.com/docs/v4/)
* [Slim Framework GitHub Repository](https://github.com/slimphp/Slim)
* [PSR-7 HTTP Message Interface](https://www.php-fig.org/psr/psr-7/)
* [PSR-15 HTTP Server Request Handlers](https://www.php-fig.org/psr/psr-15/)
