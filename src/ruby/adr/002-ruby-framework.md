# ADR: Select Framework for Building a REST API in Ruby (2025)

**Status:** Accepted
**Date:** 2025-05-26

## Context

We are starting a new REST API project in Ruby in 2025. Ruby offers several frameworks suited to API development, each with different trade-offs regarding ecosystem support, scalability, maintainability, and performance.

The most widely used Ruby frameworks for REST APIs are:

* **Rails (Ruby on Rails)**
* **Hanami**
* **Sinatra**
* **Roda**
* **Grape**

## Decision

We will use **Ruby on Rails (API-only mode)** as the framework for this project.

## Rationale

* **Maturity & Community:** Rails is the most mature Ruby web framework, with a large ecosystem and community. Most Ruby developers are familiar with Rails conventions.
* **API Mode:** Rails API-only mode strips out view and frontend components, providing a lightweight, streamlined experience for building APIs.
* **Tooling & Ecosystem:** Outstanding libraries, documentation, plugins, and built-in features (e.g., ActiveRecord, migrations, testing).
* **Scalability:** Suitable for both small APIs and large-scale production systems.
* **Hiring:** Easiest to find developers with Rails experience.
* **Support:** Rails is actively maintained and keeps up with new Ruby versions.

### Alternatives Considered

* **Hanami:** A modern, modular, and clean alternative with growing popularity. Strong architectural principles and performance. Not chosen primarily due to smaller community/ecosystem and fewer ready-to-use plugins compared to Rails.
* **Sinatra:** Ideal for ultra-lightweight or prototype APIs. Not chosen for this project, as we expect to benefit from Rails’ features and conventions in the long term.
* **Roda:** Very fast and minimalist, best suited for performance-critical or microservice use cases. Not chosen as we value ecosystem and maintainability more for this project.
* **Grape:** API-focused framework often used as a plugin in other frameworks; less common as a standalone choice.

## Consequences

* The project benefits from a mature, stable, and well-documented framework.
* Rapid development with minimal boilerplate for API needs.
* Easy onboarding of new developers due to Rails’ popularity.
* Slightly higher baseline resource usage than the most minimal frameworks, but offset by maintainability and scalability benefits.
* If future needs arise (such as adding a web frontend), Rails provides a clear upgrade path.

---

**Decision:**
**Use Ruby on Rails (API-only mode) as the framework for the new REST API. Reevaluate if project requirements shift toward minimalism or modularity (Hanami/Sinatra).**
