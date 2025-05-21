# ADR 002: Select Eclipse Temurin 21 as Java 21 Runtime Implementation

**Status:** Proposed
**Date:** 2025-05-21

**Context:**
After choosing Java 21 (LTS) for the project, the team must select a specific runtime implementation. Options include Eclipse Temurin, Amazon Corretto, Oracle JDK, GraalVM, and others. The selection should consider support, licensing, compatibility, performance (including startup time), containerization, and commercial suitability.

**Decision:**
The project will use **Eclipse Temurin 21** as the default Java runtime implementation.

**Consequences:**

* Leverages a widely adopted, open-source, community-driven JDK distribution with robust, timely updates.
* TCK-certified (Java SE compatible) and broadly supported across platforms, cloud, and container environments.
* Licensing is simple (GPLv2 with Classpath Exception), with no usage restrictions or commercial fees.
* Can switch to other OpenJDK-based distributions in the future with minimal disruption.
* The team will monitor Adoptium releases and use official Temurin container images.

**Alternatives Considered:**

* **GraalVM 21:**

  * *Strengths:* Can compile Java applications to native executables for extremely fast startup, low memory usage, and small distribution size. Valuable for serverless, CLI tools, and microservices with stringent performance requirements.
  * *Limitations:* Native image mode requires adaptation (not all Java libraries are supported natively), larger build complexity, and potentially reduced runtime flexibility. JVM mode is compatible with standard Java, but startup/memory advantages are seen mainly with native image.
  * *Use case fit:* Consider for workloads needing minimal cold start time or deployment footprint, but not as a general-purpose default for all services.
* **Amazon Corretto 21:** Free, open-source, production-ready. Strong fit for AWS deployments, with regular updates and good ecosystem support.
* **Oracle JDK 21:** Official, fully compatible, and well-supported, but requires commercial licensing for most production environments.
* **Eclipse OpenJ9 21:** Offers improved startup time and memory footprint, especially in containerized/microservices settings. Less commonly adopted, but valuable for memory-constrained environments.

**Rationale:**

* **Eclipse Temurin** is the industry standard for open-source Java deployments: stable, vendor-neutral, with broad adoption and minimal licensing risk.
* **GraalVM** is recognized for its unique native image capabilities, offering the best startup and resource usage, but requires additional engineering effort and is best reserved for specific use cases rather than as the baseline runtime for all services.

**References:**

* [Eclipse Temurin (Adoptium)](https://adoptium.net/)
* [Amazon Corretto](https://aws.amazon.com/corretto/)
* [GraalVM](https://www.graalvm.org/)
* [GraalVM Native Image](https://www.graalvm.org/reference-manual/native-image/)
* [OpenJ9](https://www.eclipse.org/openj9/)
* [Java 21 Support Matrix by Vendor](https://foojay.io/today/java-21-supported-vendors/)

---

**Note:**
For components requiring optimized startup or minimal runtime footprint (e.g., serverless functions or CLI utilities), the team will assess **GraalVM native image** as an exception, with a clear evaluation of compatibility and build pipeline complexity. For all other services, **Eclipse Temurin 21** remains the standard.
