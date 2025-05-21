# ADR 004: Asynchronous Request Handling for HTTP APIs
Status: Proposed
Date: 2025-05-21

Context:
The application must expose HTTP APIs with well-defined response times and predictable concurrency behavior. The team must choose between handling requests synchronously (thread-per-request model) or asynchronously (via @Async, CompletableFuture, etc.) to optimize throughput and resource usage.

Decision:
We will use asynchronous request handling with @Async and CompletableFuture for non-blocking, I/O-intensive operations.

Consequences:

Allows better use of thread resources when performing downstream I/O (e.g., HTTP calls, database access).

Enables scaling to higher concurrent loads without increasing the thread pool size excessively.

Requires managing thread context (e.g., security, MDC) across threads.

Adds complexity in exception handling and tracing compared to synchronous flows.

Alternatives Considered:

Synchronous (default Spring MVC): Simple, easy to debug, good for CPU-bound or low-throughput APIs. However, scales poorly under high latency.

Reactive (Spring WebFlux): Fully non-blocking and more scalable under load, but requires deeper rethinking of programming style and ecosystem dependencies (see ADR 2).

Rationale:

Async handling with CompletableFuture offers a pragmatic middle ground: scalable for high-load scenarios without the steep learning curve and full-stack migration required for reactive programming.

Allows gradual adoption in specific endpoints or services.