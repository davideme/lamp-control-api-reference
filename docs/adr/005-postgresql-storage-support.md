# ADR 005: PostgreSQL Storage Support

## Status

Accepted

## Context

The Lamp Control API Reference project currently includes multiple language implementations (C#, Go, Java, Kotlin, Python, and TypeScript) with varying levels of database support. While the project has SQL schema definitions for both MySQL and PostgreSQL in the `database/sql/` directory, not all language implementations have fully integrated PostgreSQL storage capabilities.

Key considerations:

1. **Production Database Requirements**: PostgreSQL is widely adopted in production environments for its:
   - ACID compliance and data integrity
   - Advanced features (JSONB, full-text search, window functions)
   - Strong community support and ecosystem
   - Superior scalability and performance characteristics
   
2. **Cross-Implementation Consistency**: A reference API project should demonstrate consistent database integration patterns across all major language implementations

3. **Development and Testing**: PostgreSQL support enables:
   - Realistic local development environments
   - Integration testing with production-like databases
   - Demonstration of proper connection pooling and transaction management
   - Best practices for database migrations and schema management

4. **Existing Infrastructure**: The project already includes:
   - PostgreSQL schema definition at `database/sql/postgresql/schema.sql`
   - Docker Compose configuration for local PostgreSQL instances
   - Documentation for database setup and configuration

## Decision

We will implement **PostgreSQL storage support** for the following language implementations as a priority:

- **C#**
- **Go**
- **Java**
- **Kotlin**
- **Python**
- **TypeScript**

### Implementation Requirements

Each implementation must include:

1. **Database Connection Management**
   - Connection string configuration via environment variables
   - Connection pooling with appropriate size limits
   - Proper connection lifecycle management (open/close)
   - Health check endpoints for database connectivity

2. **Repository/Data Access Layer**
   - Abstraction layer for database operations (Repository pattern or equivalent)
   - CRUD operations for lamp resources:
     - Create new lamp records
     - Read lamp by ID and list all lamps with pagination
     - Update lamp state (on/off status)
     - Delete lamp records
   - Proper error handling and exception translation

3. **Schema Management**
   - Use of the existing `database/sql/postgresql/schema.sql` schema
   - Migration strategy documentation (manual or automated)
   - Support for schema initialization in test environments

4. **Data Mapping**
   - Domain models mapped to database tables
   - Proper type conversion (language types ↔ PostgreSQL types)
   - Handling of nullable fields and default values
   - Timestamp/datetime handling with timezone awareness

5. **Transaction Support**
   - Transaction management for multi-step operations
   - Rollback on errors
   - Proper isolation level configuration

6. **Testing**
   - Integration tests using real PostgreSQL instances (Docker containers)
   - Test data fixtures and cleanup strategies
   - Verification of CRUD operations
   - Connection pooling and concurrency tests

7. **Documentation**
   - Configuration examples in README files
   - Environment variable documentation
   - Database setup instructions
   - Connection string format examples

### Configuration Standards

All implementations should support these environment variables:

- `DATABASE_URL` or `POSTGRES_CONNECTION_STRING`: Full connection string
- `DB_HOST`: PostgreSQL host (default: localhost)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: lampcontrol)
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password
- `DB_POOL_MIN_SIZE`: Minimum connection pool size
- `DB_POOL_MAX_SIZE`: Maximum connection pool size

## Rationale

### Why PostgreSQL?

1. **Industry Standard**: PostgreSQL is one of the most popular open-source relational databases
2. **Feature Rich**: Provides advanced features needed for modern applications
3. **Production Ready**: Used by major companies for mission-critical applications
4. **Cross-Platform**: Runs on all major operating systems
5. **Existing Schema**: Project already has PostgreSQL schema defined

### Why These Languages?

The selected languages (C#, Go, Java, Kotlin, Python, TypeScript) represent:
- The most mature implementations in the project
- The most commonly used languages for backend API development
- Languages with robust PostgreSQL client libraries and ORMs
- Languages that can serve as reference examples for other implementations

### Benefits

1. **Demonstration Value**: Shows best practices for database integration in each language
2. **Testing Capability**: Enables comprehensive integration testing
3. **Production Parity**: Aligns development/test environments with production scenarios
4. **Learning Resource**: Provides reference implementations for developers learning database integration
5. **Consistency**: Ensures all major implementations support the same database backend

## Consequences

### Positive

- **Enhanced Reference Implementation**: More complete and production-ready examples
- **Improved Testing**: Integration tests can verify end-to-end data persistence
- **Better Documentation**: Forces documentation of database setup and configuration
- **Realistic Examples**: Demonstrates real-world patterns for database access
- **Consistency**: Common database backend across implementations enables comparison

### Negative

- **Implementation Effort**: Requires significant development time across six languages
- **Maintenance Burden**: Database code requires ongoing maintenance and updates
- **Testing Complexity**: Integration tests need PostgreSQL containers or instances
- **Documentation Requirements**: More setup steps for contributors
- **Dependency Management**: Additional dependencies for PostgreSQL clients and ORMs

### Neutral

- **Schema Evolution**: Future schema changes must be implemented across all six languages
- **Performance Tuning**: Each implementation may require language-specific optimization
- **Error Handling**: Different languages have different patterns for database errors
- **Testing Strategy**: Need to balance unit tests vs. integration tests with database

## References

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Project Schema: database/sql/postgresql/schema.sql](../../database/sql/postgresql/schema.sql)
- [Database Setup Documentation](../../database/README.md)
- [Docker Compose Configuration](../../docker-compose.yml)

## Related ADRs

- ADR 002: Pagination Strategy (database queries must support pagination)
- ADR 003: Offline-First API (database serves as source of truth)
- ADR 004: Schemathesis Integration (database integration must not break API contracts)
