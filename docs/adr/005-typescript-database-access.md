# ADR-005: TypeScript Database Access

**Status:** Proposed

**Date:** 2025-04-18

## Context

The reference implementation must support both relational (MySQL, PostgreSQL) and NoSQL (MongoDB) databases. We need to select appropriate Object-Relational Mappers (ORMs) or Object-Document Mappers (ODMs) for TypeScript to provide a consistent, type-safe, and efficient way to interact with these databases.

## Decision

We will use the following libraries for database access:

-   **Relational (MySQL/PostgreSQL):** **Prisma ORM**
    -   _Rationale:_ Prisma provides exceptional type safety derived from the database schema, a powerful and intuitive query API, built-in migration tooling (`prisma migrate`), and excellent TypeScript integration. It simplifies database interactions significantly compared to traditional query builders or basic drivers.
-   **NoSQL (MongoDB):** **Mongoose ODM**
    -   _Rationale:_ Mongoose is the de facto standard ODM for MongoDB in the Node.js ecosystem. It provides schema definition and validation, query building capabilities, middleware hooks, and population for related data, offering structure and convenience on top of the native MongoDB driver.

## Consequences

-   **Pros:**
    -   Provides strong type safety for both relational and document databases within the TypeScript environment.
    -   Leverages mature and popular libraries specific to each database type.
    -   Prisma's migration tooling simplifies schema evolution for relational databases.
    -   Mongoose offers helpful abstractions and validation for MongoDB.
    -   Reduces boilerplate code for common database operations.
-   **Cons:**
    -   Introduces two distinct data access patterns and APIs (Prisma and Mongoose) that developers need to learn.
    -   Adds dependencies to the project.
    -   ORMs/ODMs can sometimes introduce a layer of abstraction that might obscure underlying database behavior or performance characteristics if not used carefully.

## Alternatives Considered

-   **Relational ORM Alternatives:**
    -   _TypeORM:_ Another popular TypeScript ORM, uses decorators heavily.
    -   _Sequelize:_ A mature, widely used ORM, originally JavaScript-focused with TypeScript support added later.
    -   _Knex.js:_ A query builder, less abstraction than a full ORM, requires more manual mapping.
-   **NoSQL (MongoDB) Alternatives:**
    -   _Native MongoDB Node.js Driver:_ Provides direct access but requires manual schema handling, validation, and object mapping.
    -   _Typegoose:_ Builds on Mongoose, using classes and decorators for schema definition.
-   **Using a Single ORM for Both (e.g., Prisma for MongoDB):** Prisma's MongoDB support is newer than its relational support and might have different feature parity or maturity compared to Mongoose, which is specifically designed for MongoDB.

## References

-   [Prisma ORM](https://www.prisma.io/)
-   [Mongoose ODM](https://mongoosejs.com/)
-   [TypeORM](https://typeorm.io/)
-   [Sequelize](https://sequelize.org/)
-   [Knex.js](https://knexjs.org/)
-   [MongoDB Node.js Driver](https://www.mongodb.com/docs/drivers/node/current/)
-   [Typegoose](https://typegoose.github.io/typegoose/) 