# ADR 001: Adopt Java 21 (LTS) as Baseline Version for New Project

**Status:** Proposed
**Date:** 2025-05-21

**Context:**
A new project is being initiated. The team must decide which Java version to adopt as the baseline for development and production environments. The decision should consider long-term support, compatibility, ecosystem support, and organizational requirements.

**Decision:**
The project will use **Java 21 (LTS)** as the standard runtime and language version.

**Release and Lifecycle:**

* **Release Date:** Java 21 was released on September 19, 2023.
* **Long-Term Support:** Java 21 is a Long-Term Support (LTS) version, with free public updates from most vendors expected until September 2026 and extended support available through at least September 2031 (vendor-dependent).
* **Next LTS Release:** The next planned LTS version is Java 25, scheduled for release in September 2025.

**Consequences:**

* The project will benefit from the latest language features, performance improvements, and security updates provided by Java 21.
* Long-term support (LTS) guarantees stability and ongoing maintenance for at least several years.
* Compatibility with up-to-date libraries, frameworks, and cloud platforms.
* Team members must ensure local and CI/CD environments are configured for Java 21.
* Dependencies that do not support Java 21 will need to be evaluated or replaced.

**Alternatives Considered:**

* **Java 17 (LTS):** Mature, widely adopted, and supported by all current libraries. However, it lacks some improvements and features introduced in Java 21.
* **Java 22 (non-LTS):** Provides the most recent features but lacks long-term support and stability guarantees.
* **Older LTS versions (Java 11):** Increasingly outdated, less attractive in terms of features and support.

**Rationale:**

* Java 21 is the latest LTS release, balancing stability and access to modern features.
* Supported by major cloud vendors and toolchains.
* Adoption aligns with industry best practices for new greenfield projects.

**References:**

* [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
* [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
* [Java Release Cadence](https://openjdk.org/projects/jdk/)
