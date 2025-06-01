# ADR 006: Imperative Async vs Reactive (Flux/Mono) Stack
Status: Proposed
Date: 2025-05-21

Context:
We must decide whether to build the application using Spring Web MVC (imperative model with optional async) or Spring WebFlux (reactive stack using Project Reactor). The choice affects the entire programming model, compatibility with libraries, and operational observability.

Decision:
We will use Spring MVC with optional asynchronous programming (CompletableFuture). We will not adopt Spring WebFlux for the initial version of this project.

Consequences:

Keeps compatibility with a broad set of mature Spring ecosystem libraries (e.g., Spring Data JPA, security, actuator).

Easier to onboard developers familiar with traditional Spring and Java paradigms.

Avoids reactive-specific complexity in error handling, flow control (backpressure), and debugging.

May not fully leverage non-blocking I/O benefits for extreme scalability; those can be revisited later for targeted services.

Alternatives Considered:

Spring WebFlux (Reactive + Flux/Mono):

Pros: Fully non-blocking, better for thousands of concurrent I/O-bound connections (e.g., APIs, gateways, streaming).

Cons: Requires reactive-compatible libraries, increased complexity in flow control and testability, less mature debugging tools.

Rationale:

The primary system workload is request/response with moderate to high I/O, not streaming or high-concurrency messaging.

Async Spring MVC allows us to scale without full-stack reengineering.

Reactive programming may be re-evaluated in future services (e.g., event-driven pipelines or streaming APIs) as needs evolve.

References:

Spring MVC Async Support

Spring WebFlux vs Spring MVC

Reactor Project
