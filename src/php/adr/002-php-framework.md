# ADR-002: Select PHP Framework and Version for REST API

**Status:** Accepted
**Date:** 2025-05-26

## Context

We are initiating a new PHP project with the primary objective of building a robust, maintainable, and scalable REST API. The framework choice should enable rapid development, ensure security, and provide strong community and ecosystem support over the coming years.

## Decision

We will use **Laravel 12** as the framework for our REST API.

* **Version:** Laravel 12 (released on February 24, 2025)
* **PHP Compatibility:** PHP 8.2, 8.3, and 8.4

## Rationale

* **Latest Stable Release:** Laravel 12 is the most recent stable version, ensuring access to the latest features and improvements.
* **Support Timeline:** Laravel 12 will receive bug fixes until August 13, 2026, and security updates until February 24, 2027.
* **Enhanced Features:** Laravel 12 introduces improved application structure, new starter kits for Vue, React, and Livewire, and advanced API features like GraphQL support and better versioning.
* **Community and Ecosystem:** Laravel has a large and active community, with extensive documentation and a rich ecosystem of packages and tools.
* **Performance and Scalability:** Laravel 12 includes performance optimizations and supports asynchronous processing via Laravel Octane for high-throughput API needs.

## Alternatives Considered

* **Laravel 11:** While still supported, Laravel 11 will receive bug fixes only until August 5, 2025, and security updates until February 3, 2026, making Laravel 12 a more future-proof choice.
* **Symfony:** Highly modular and robust, ideal for complex, enterprise APIs, but has a steeper learning curve and requires more boilerplate.
* **Slim:** Lightweight and fast, suited for microservices, but lacks many features and requires additional packages for full API functionality.
* **API Platform:** Built on Symfony, specialized for API-first projects (REST & GraphQL), but introduces additional complexity.

## Risks

* **Ecosystem Maturity:** As Laravel 12 is a recent release, some third-party packages may not yet be fully compatible.
* **Learning Curve:** New features and changes in Laravel 12 may require additional learning and adaptation time for the development team.

## References

* [Laravel 12 Release Notes](https://laravel.com/docs/12.x/releases)
* [Laravel Versions and Support Policy](https://laravelversions.com/en)
* [Laravel 12 Features and Updates](https://www.bacancytechnology.com/blog/laravel-12-features-updates)
