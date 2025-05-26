# ADR 001: Select Ruby Version for New Project

## Status

Accepted

## Date

2025-05-26

## Context

We are starting a new Ruby project in 2025 and must choose the Ruby version to use as our runtime. The Ruby language releases a new major version every December and maintains each release for roughly three years, with the last two to three versions receiving security and critical bug fixes. Ruby does **not** have a formal LTS (Long-Term Support) program, but recent versions are actively maintained.

## Decision

We will use **Ruby 3.4.x** as the language runtime for this project.

## Rationale

* **Latest stable release:** Ruby 3.4, released December 25, 2024, is the newest stable version and includes the latest language features, performance improvements, and security fixes.
* **Community and ecosystem support:** All major gems and frameworks are compatible with Ruby 3.4, ensuring smooth development and maintenance.
* **Maintenance and security:** Ruby 3.4 will receive official updates and security patches until at least March 2028, providing a stable window for project development and deployment.
* **No formal LTS:** Ruby does not designate LTS versions. The best practice is to use the latest stable release for new projects.
* **Forward compatibility:** Using the latest version reduces the technical debt and effort required for future upgrades.

## Consequences

* The project will benefit from new features and improved performance introduced in Ruby 3.4.
* We will need to monitor future Ruby releases and plan for regular upgrades (every 2–3 years) to remain within the supported window.
* Older systems and gems that do not support Ruby 3.4 may need to be upgraded or replaced.

## Alternatives Considered

* **Ruby 3.3 or 3.2:** Would extend compatibility for teams with strict dependencies, but these versions will reach end-of-life sooner and lack the latest features and improvements.
* **Waiting for a future release:** Delaying would not be practical and would result in missing the benefits of current improvements.

---

**Decision:**
**Use Ruby 3.4.x as the runtime for all new Ruby projects started in 2025.**