# ADR: Select ASP.NET Core (.NET 8 LTS) for REST API Development

**Status:** Accepted
**Date:** 2025-05-26

## Context

We need to select a framework for developing a new REST API using C#. The decision impacts maintainability, scalability, developer productivity, long-term support, and ecosystem compatibility.

## Decision

We will use **ASP.NET Core** (the latest version aligned with **.NET 8 LTS**) as the framework for REST API development.

## Rationale

* **Performance and Reliability:** ASP.NET Core is recognized for its high performance, reliability, and efficiency in building scalable APIs.
* **Long-Term Support:** .NET 8 is an LTS release, ensuring support and security updates through at least November 2026.
* **Modern Features:** Supports minimal APIs, OpenAPI (Swagger) integration, built-in dependency injection, and modular middleware.
* **Cross-Platform:** Runs on Windows, Linux, and macOS, providing flexibility for deployment and development.
* **Ecosystem and Community:** Extensive documentation, active community, and strong integration with popular tools and cloud platforms.
* **Future Proofing:** Regularly updated by Microsoft, ensuring compatibility with future releases and ecosystem growth.

## Alternatives Considered

* **ServiceStack:** Commercial license required, smaller ecosystem, less mainstream.
* **NancyFX:** No longer actively maintained.
* **Carter:** Inspired by minimal APIs but less widely adopted and smaller community.
* **Custom Framework:** Higher maintenance cost and risk, with limited support and ecosystem.

## Consequences

* Leverages a widely adopted and supported framework.
* Ensures maintainability, scalability, and developer productivity.
* Provides access to a large talent pool and established best practices.
* Facilitates integration with modern development tools, CI/CD systems, and cloud platforms.