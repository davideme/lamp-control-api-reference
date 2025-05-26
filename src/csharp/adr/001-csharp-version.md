# ADR: Choose C# 12.0 and .NET 8 for New Project

**Status:** Accepted
**Date:** 2025-05-26

## Context

We need to select the version of C# and .NET for our new project. The choice affects language features, long-term support, tooling compatibility, and future maintenance.

## Decision

We will use **C# 12.0** with **.NET 8** (LTS) for all new development.

## Rationale

* **Long-Term Support (LTS):** .NET 8 is the latest LTS release (supported until November 2026), ensuring ongoing security updates and bug fixes.
* **Latest Language Features:** C# 12.0 brings modern productivity enhancements, performance improvements, and better maintainability.
* **Ecosystem and Tooling:** Most major libraries and frameworks support .NET 8. The developer ecosystem (IDEs, build systems, CI/CD tools) is aligned with this version.
* **Stability:** LTS releases are recommended by Microsoft for production use, providing stability over standard-term releases.

## Alternatives Considered

* **.NET 6 / C# 10.0:** Still LTS but reaches end of support in November 2024, making it unsuitable for new projects starting in late 2025.
* **.NET 7 / C# 11.0:** Not an LTS release. Support ended in May 2024.
* **.NET 9 (Preview):** Not recommended for production use until official release and stabilization.

## Consequences

* New features in .NET 8 and C# 12.0 will be available to the team.
* The project will have support and security updates through at least November 2026.
* Upgrading from .NET 8 to future LTS versions should be straightforward due to Microsoft’s established upgrade paths.
