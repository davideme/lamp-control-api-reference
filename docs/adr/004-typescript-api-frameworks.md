# ADR 004: TypeScript API Frameworks

**Status:** Proposed

**Date:** 2025-04-18

## Context

The project requires implementing three distinct API styles: REST, GraphQL, and gRPC. We need to select appropriate, well-supported frameworks or libraries within the TypeScript/Node.js ecosystem for each style to ensure consistency and leverage community standards.

## Decision

We will use the following frameworks for the respective API styles:

-   **REST:** **Express.js**
    -   _Rationale:_ Mature, highly popular, minimalist, and unopinionated web framework with a vast ecosystem. Well-suited for building traditional REST APIs. We will supplement it with `swagger-ui-express` and an OpenAPI 3.0 specification for documentation.
-   **GraphQL:** **Apollo Server** (specifically `@apollo/server`)
    -   _Rationale:_ A comprehensive and widely adopted GraphQL server implementation. Integrates seamlessly with Express.js and provides features like caching, subscriptions (if needed later), and a robust tooling ecosystem (Apollo Studio).
-   **gRPC:** **`@grpc/grpc-js`** with **Protocol Buffers**
    -   _Rationale:_ `@grpc/grpc-js` is the official, modern gRPC library for Node.js, maintained by the gRPC team. Protocol Buffers provide an efficient, language-neutral mechanism for serializing structured data and defining service interfaces. We will use `@grpc/proto-loader` for dynamic loading and `ts-proto` for generating TypeScript types from `.proto` files.

## Consequences

-   **Pros:**
    -   Utilizes popular, well-documented, and actively maintained libraries for each API style.
    -   Leverages the strengths of each chosen framework (Express flexibility, Apollo's GraphQL features, gRPC's performance and strong typing).
    -   Clear separation of concerns for each API implementation.
    -   `ts-proto` enhances type safety for gRPC development.
-   **Cons:**
    -   Requires integrating and managing three different server setups/entry points (though Apollo can integrate into Express).
    -   Introduces dependencies specific to each framework.
    -   Potential boilerplate code for setting up each API style.

## Alternatives Considered

-   **Unified Frameworks:**
    -   _NestJS:_ An opinionated framework that offers modules for REST, GraphQL, and gRPC out-of-the-box. Considered potentially too opinionated for a reference implementation aiming to show specific library usage.
-   **REST Alternatives:**
    -   _Fastify:_ Known for high performance and low overhead, schema-based.
    -   _Koa:_ More modern, middleware-focused successor to Express.
-   **GraphQL Alternatives:**
    -   _`graphql-yoga`:_ Another popular, spec-compliant GraphQL server.
    -   _`express-graphql`:_ A simpler middleware, less feature-rich than Apollo Server.
-   **gRPC Alternatives:**
    -   Using the `google-protobuf` library directly with `grpc-js` (more manual setup for type generation).

## References

-   [Express.js](https://expressjs.com/)
-   [Apollo Server](https://www.apollographql.com/docs/apollo-server/)
-   [`@grpc/grpc-js`](https://github.com/grpc/grpc-node/tree/master/packages/grpc-js)
-   [Protocol Buffers](https://protobuf.dev/)
-   [`@grpc/proto-loader`](https://github.com/grpc/grpc-node/tree/master/packages/proto-loader)
-   [`ts-proto`](https://github.com/stephenh/ts-proto)
-   [OpenAPI Initiative](https://www.openapis.org/)
-   [`swagger-ui-express`](https://github.com/scottie1984/swagger-ui-express) 