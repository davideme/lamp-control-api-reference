# ADR 009: TypeScript Testing Framework

**Status:** Proposed

**Date:** 2025-04-18

## Context

Comprehensive testing (unit, integration, potentially end-to-end) is crucial for ensuring the quality, correctness, and reliability of the API implementation. We need a testing framework that works well with TypeScript/Node.js, supports various testing types, and provides features like mocking, assertions, and code coverage reporting.

## Decision

We will use the following combination for testing the TypeScript implementation:

-   **Primary Framework:** **Jest**
    -   _Rationale:_ Jest is a very popular, "batteries-included" testing framework developed by Facebook. It offers an integrated experience with a test runner, assertion library, mocking capabilities (including auto-mocking), and built-in code coverage reporting. Its widespread adoption, good TypeScript support (via `ts-jest` or Babel), and features like snapshot testing make it a strong choice for general testing.
-   **HTTP API Testing:** **Supertest**
    -   _Rationale:_ Supertest provides a high-level abstraction for testing HTTP APIs. It allows making requests against an HTTP server (like an Express app) within the test environment and provides a fluent API for asserting responses (status codes, headers, bodies). It integrates seamlessly with Jest for integration testing of REST and GraphQL endpoints.

## Consequences

-   **Pros:**
    -   Jest provides an all-in-one testing solution, simplifying setup.
    -   Excellent support for TypeScript via `ts-jest`.
    -   Powerful mocking and assertion capabilities.
    -   Built-in code coverage reporting.
    -   Supertest simplifies integration testing of HTTP endpoints.
    -   Large community and extensive documentation for both libraries.
-   **Cons:**
    -   Jest can sometimes be perceived as slightly heavier or slower than alternatives like Vitest, especially for very large projects.
    -   Requires configuration (`jest.config.js`, potentially `ts-jest` setup).
    -   Adds dependencies.

## Alternatives Considered

-   **Mocha + Chai + Sinon:** A classic combination. Mocha is a test runner, Chai is an assertion library, and Sinon is used for mocking/spying. More modular but requires combining and configuring multiple libraries.
-   **Vitest:** A newer testing framework designed with Vite in mind. Offers Jest-compatible API, potentially faster performance, but might be slightly less mature or have a smaller ecosystem than Jest currently.
-   **Node.js built-in test runner (`node:test`):** A recent addition to Node.js. Still evolving and lacks the rich feature set and ecosystem of Jest or Mocha.
-   **AVA:** Another test runner known for running tests concurrently.

## References

-   [Jest](https://jestjs.io/)
-   [`ts-jest`](https://kulshekhar.github.io/ts-jest/)
-   [Supertest](https://github.com/ladjs/supertest)
-   [Mocha](https://mochajs.org/)
-   [Chai](https://www.chaijs.com/)
-   [Sinon](https://sinonjs.org/)
-   [Vitest](https://vitest.dev/)
-   [Node.js Test Runner](https://nodejs.org/api/test.html)
-   [AVA](https://github.com/avajs/ava) 