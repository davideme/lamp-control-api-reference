# ADR 006: PostgreSQL Storage with Exposed ORM

## Status

Accepted

## Context

The Kotlin implementation of the Lamp Control API currently uses an in-memory repository for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with Kotlin-idiomatic patterns, coroutine support, and type-safe database access.

### Current State

- **Framework**: Ktor 3.0.2 with Kotlin serialization
- **Architecture**: `LampRepository` interface with `InMemoryLampRepository` implementation
- **Storage**: In-memory concurrent map with suspend functions
- **Dependencies**: Ktor server components, Kotlin coroutines

### Requirements

1. **Kotlin-Idiomatic**: DSL-based API that feels native to Kotlin
2. **Type Safety**: Compile-time query validation with Kotlin types
3. **Coroutine Support**: Suspend functions for async database operations
4. **Performance**: Efficient queries, connection pooling
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with real PostgreSQL instances

### Technology Landscape (2025-2026)

**Exposed (JetBrains)**
- Official Kotlin SQL library from JetBrains
- Type-safe DSL for SQL queries
- Coroutine support with `suspendedTransaction`
- Dual API: DSL (type-safe SQL) + DAO (ORM-like)
- 8k+ GitHub stars, actively maintained
- Lightweight, minimal overhead

**Ktorm**
- Kotlin ORM with fluent API
- Strong typing, entity sequences
- Good documentation
- Smaller community (2k stars)

**Spring Data JPA (Kotlin)**
- Full Spring ecosystem
- Kotlin extensions available
- Heavier framework (brings entire Spring Boot)

**R2DBC**
- Reactive database connectivity
- Async/non-blocking
- Requires reactive stack (less mature)

## Decision

We will implement **Exposed ORM (JetBrains) with HikariCP** as the PostgreSQL data access layer for the Kotlin Lamp Control API implementation.

### Architecture

```
Routes → LampRepository → ExposedLampRepository → Exposed DSL → HikariCP → PostgreSQL
```

### Core Components

#### 1. **Database Configuration**

```kotlin
package com.lampcontrol.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    fun init(config: DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            driverClassName = "org.postgresql.Driver"
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minPoolSize
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            poolName = "LampControlHikariCP"
            
            // PostgreSQL optimizations
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
    }
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int = 20,
    val minPoolSize: Int = 5
)
```

#### 2. **Table Definition (DSL)**

```kotlin
package com.lampcontrol.api.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.UUID

object LampsTable : IdTable<UUID>("lamps") {
    override val id: Column<EntityID<UUID>> = uuid("id").entityId()
    val isOn = bool("is_on")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_lamps_is_on", false, isOn)
        index("idx_lamps_created_at", false, createdAt)
        index("idx_lamps_deleted_at", false, deletedAt)
    }
}
```

#### 3. **Domain Model**

```kotlin
package com.lampcontrol.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Lamp(
    val id: UUID,
    val isOn: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null
)
```

#### 4. **Repository Implementation**

```kotlin
package com.lampcontrol.api.repository

import com.lampcontrol.api.db.LampsTable
import com.lampcontrol.api.model.Lamp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExposedLampRepository : LampRepository {

    override suspend fun create(lamp: Lamp): Lamp = dbQuery {
        val now = Clock.System.now().toJavaInstant()
        
        LampsTable.insert {
            it[id] = lamp.id
            it[isOn] = lamp.isOn
            it[createdAt] = now
            it[updatedAt] = now
        }
        
        lamp.copy(
            createdAt = now.toKotlinInstant(),
            updatedAt = now.toKotlinInstant()
        )
    }

    override suspend fun getById(id: UUID): Lamp? = dbQuery {
        LampsTable
            .select { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() }
            .map { it.toLamp() }
            .singleOrNull()
    }

    override suspend fun getAll(offset: Int, limit: Int): List<Lamp> = dbQuery {
        LampsTable
            .select { LampsTable.deletedAt.isNull() }
            .orderBy(LampsTable.createdAt to SortOrder.ASC)
            .limit(limit, offset.toLong())
            .map { it.toLamp() }
    }

    override suspend fun update(id: UUID, lamp: Lamp): Lamp? = dbQuery {
        val now = Clock.System.now().toJavaInstant()
        
        val updated = LampsTable.update({ 
            (LampsTable.id eq id) and LampsTable.deletedAt.isNull() 
        }) {
            it[isOn] = lamp.isOn
            it[updatedAt] = now
        }
        
        if (updated > 0) {
            getById(id)
        } else {
            null
        }
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val now = Clock.System.now().toJavaInstant()
        
        LampsTable.update({ 
            (LampsTable.id eq id) and LampsTable.deletedAt.isNull() 
        }) {
            it[deletedAt] = now
        } > 0
    }

    override suspend fun count(): Long = dbQuery {
        LampsTable
            .select { LampsTable.deletedAt.isNull() }
            .count()
    }

    // Helper function to map ResultRow to Lamp
    private fun ResultRow.toLamp(): Lamp = Lamp(
        id = this[LampsTable.id].value,
        isOn = this[LampsTable.isOn],
        createdAt = this[LampsTable.createdAt].toKotlinInstant(),
        updatedAt = this[LampsTable.updatedAt].toKotlinInstant(),
        deletedAt = this[LampsTable.deletedAt]?.toKotlinInstant()
    )

    // Coroutine-aware database query
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
```

#### 5. **Repository Interface**

```kotlin
package com.lampcontrol.api.repository

import com.lampcontrol.api.model.Lamp
import java.util.UUID

interface LampRepository {
    suspend fun create(lamp: Lamp): Lamp
    suspend fun getById(id: UUID): Lamp?
    suspend fun getAll(offset: Int = 0, limit: Int = 100): List<Lamp>
    suspend fun update(id: UUID, lamp: Lamp): Lamp?
    suspend fun delete(id: UUID): Boolean
    suspend fun count(): Long
}
```

#### 6. **Ktor Application Setup**

```kotlin
package com.lampcontrol.api

import com.lampcontrol.api.config.DatabaseConfig
import com.lampcontrol.api.config.DatabaseFactory
import com.lampcontrol.api.repository.ExposedLampRepository
import com.lampcontrol.api.routes.lampRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Initialize database
    val config = DatabaseConfig(
        jdbcUrl = environment.config.property("database.url").getString(),
        username = environment.config.property("database.username").getString(),
        password = environment.config.property("database.password").getString(),
        maxPoolSize = environment.config.propertyOrNull("database.maxPoolSize")?.getString()?.toInt() ?: 20,
        minPoolSize = environment.config.propertyOrNull("database.minPoolSize")?.getString()?.toInt() ?: 5
    )
    
    DatabaseFactory.init(config)
    
    // Create repository
    val lampRepository = ExposedLampRepository()
    
    // Configure routing
    routing {
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }
        
        lampRoutes(lampRepository)
    }
}
```

#### 7. **Route Handlers**

```kotlin
package com.lampcontrol.api.routes

import com.lampcontrol.api.model.Lamp
import com.lampcontrol.api.repository.LampRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID

fun Route.lampRoutes(repository: LampRepository) {
    route("/lamps") {
        
        // Create lamp
        post {
            val lampRequest = call.receive<CreateLampRequest>()
            val lamp = Lamp(
                id = UUID.randomUUID(),
                isOn = lampRequest.isOn,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )
            
            val created = repository.create(lamp)
            call.respond(HttpStatusCode.Created, created)
        }
        
        // Get all lamps
        get {
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            
            val lamps = repository.getAll(offset, limit)
            call.respond(lamps)
        }
        
        // Get lamp by ID
        get("/{id}") {
            val id = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val lamp = repository.getById(id)
            if (lamp != null) {
                call.respond(lamp)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        
        // Update lamp
        put("/{id}") {
            val id = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val updateRequest = call.receive<UpdateLampRequest>()
            val existingLamp = repository.getById(id)
                ?: return@put call.respond(HttpStatusCode.NotFound)
            
            val updated = repository.update(id, existingLamp.copy(isOn = updateRequest.isOn))
            if (updated != null) {
                call.respond(updated)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        
        // Delete lamp
        delete("/{id}") {
            val id = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val deleted = repository.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

data class CreateLampRequest(val isOn: Boolean)
data class UpdateLampRequest(val isOn: Boolean)
```

### Configuration

#### **application.conf**

```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
    application {
        modules = [com.lampcontrol.api.ApplicationKt.module]
    }
}

database {
    url = "jdbc:postgresql://localhost:5432/lampcontrol"
    url = ${?DATABASE_URL}
    username = "lampuser"
    username = ${?DB_USER}
    password = "lamppass"
    password = ${?DB_PASSWORD}
    maxPoolSize = 20
    maxPoolSize = ${?DB_POOL_MAX_SIZE}
    minPoolSize = 5
    minPoolSize = ${?DB_POOL_MIN_SIZE}
}
```

#### **Environment Variables**

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/lampcontrol
DB_USER=lampuser
DB_PASSWORD=lamppass
DB_POOL_MAX_SIZE=25
DB_POOL_MIN_SIZE=5
PORT=8080
```

### Dependencies

#### **build.gradle.kts**

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:3.0.2")
    implementation("io.ktor:ktor-server-netty:3.0.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.2")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.47.0")

    // PostgreSQL & Connection Pooling
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Kotlin DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.ktor:ktor-server-test-host:3.0.2")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}
```

### Migration Strategy

#### **Flyway Integration** (Recommended)

```kotlin
// build.gradle.kts
plugins {
    id("org.flywaydb.flyway") version "10.4.1"
}

flyway {
    url = "jdbc:postgresql://localhost:5432/lampcontrol"
    user = "lampuser"
    password = "lamppass"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
```

```bash
# Create migration
mkdir -p src/main/resources/db/migration

# Initial schema migration
cat << 'EOF' > src/main/resources/db/migration/V1__Initial_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_lamps_is_on ON lamps (is_on);
CREATE INDEX IF NOT EXISTS idx_lamps_created_at ON lamps (created_at);
CREATE INDEX IF NOT EXISTS idx_lamps_deleted_at ON lamps (deleted_at);
EOF

# Run migration
./gradlew flywayMigrate
```

#### **Exposed SchemaUtils** (Alternative)

```kotlin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDatabase() {
    transaction {
        // Create tables if they don't exist
        SchemaUtils.create(LampsTable)
    }
}
```

### Testing Strategy

#### **Integration Test with Testcontainers**

```kotlin
package com.lampcontrol.api.repository

import com.lampcontrol.api.config.DatabaseConfig
import com.lampcontrol.api.config.DatabaseFactory
import com.lampcontrol.api.db.LampsTable
import com.lampcontrol.api.model.Lamp
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExposedLampRepositoryTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("lampcontrol_test")
            withUsername("test")
            withPassword("test")
        }
    }

    private lateinit var repository: ExposedLampRepository

    @BeforeAll
    fun setup() {
        val config = DatabaseConfig(
            jdbcUrl = postgres.jdbcUrl,
            username = postgres.username,
            password = postgres.password
        )
        
        DatabaseFactory.init(config)
        
        transaction {
            SchemaUtils.create(LampsTable)
        }
        
        repository = ExposedLampRepository()
    }

    @AfterEach
    fun cleanup() = runBlocking {
        transaction {
            LampsTable.deleteAll()
        }
    }

    @Test
    fun `should create and retrieve lamp`() = runBlocking {
        // Arrange
        val lamp = Lamp(
            id = UUID.randomUUID(),
            isOn = true,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        // Act
        val created = repository.create(lamp)
        val retrieved = repository.getById(created.id)

        // Assert
        assertNotNull(retrieved)
        assertEquals(lamp.id, retrieved.id)
        assertTrue(retrieved.isOn)
    }

    @Test
    fun `should return null for non-existent lamp`() = runBlocking {
        // Act
        val result = repository.getById(UUID.randomUUID())

        // Assert
        assertNull(result)
    }

    @Test
    fun `should update lamp status`() = runBlocking {
        // Arrange
        val lamp = Lamp(
            id = UUID.randomUUID(),
            isOn = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val created = repository.create(lamp)

        // Act
        val updated = repository.update(created.id, created.copy(isOn = true))

        // Assert
        assertNotNull(updated)
        assertTrue(updated.isOn)
    }

    @Test
    fun `should soft delete lamp`() = runBlocking {
        // Arrange
        val lamp = Lamp(
            id = UUID.randomUUID(),
            isOn = true,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val created = repository.create(lamp)

        // Act
        val deleted = repository.delete(created.id)
        val retrieved = repository.getById(created.id)

        // Assert
        assertTrue(deleted)
        assertNull(retrieved) // Soft deleted, so not found
    }

    @Test
    fun `should get all lamps with pagination`() = runBlocking {
        // Arrange
        repeat(5) {
            repository.create(
                Lamp(
                    id = UUID.randomUUID(),
                    isOn = it % 2 == 0,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
            )
        }

        // Act
        val lamps = repository.getAll(offset = 0, limit = 3)

        // Assert
        assertEquals(3, lamps.size)
    }
}
```

### Performance Optimizations

#### **1. Batch Operations**

```kotlin
suspend fun createBatch(lamps: List<Lamp>): List<UUID> = dbQuery {
    val now = Clock.System.now().toJavaInstant()
    
    LampsTable.batchInsert(lamps) { lamp ->
        this[LampsTable.id] = lamp.id
        this[LampsTable.isOn] = lamp.isOn
        this[LampsTable.createdAt] = now
        this[LampsTable.updatedAt] = now
    }.map { it[LampsTable.id].value }
}
```

#### **2. Transaction Management**

```kotlin
// Explicit transaction for multiple operations
suspend fun complexOperation(id: UUID, newStatus: Boolean) = dbQuery {
    transaction {
        val lamp = getLampById(id)
        updateLampStatus(id, newStatus)
        logStatusChange(lamp, newStatus)
    }
}
```

#### **3. Query Optimization**

```kotlin
// Efficient count query
suspend fun countByStatus(isOn: Boolean): Long = dbQuery {
    LampsTable
        .select { (LampsTable.isOn eq isOn) and LampsTable.deletedAt.isNull() }
        .count()
}

// Projection for partial data
suspend fun getAllIds(): List<UUID> = dbQuery {
    LampsTable
        .slice(LampsTable.id)
        .selectAll()
        .map { it[LampsTable.id].value }
}
```

## Rationale

### Why Exposed?

1. **Kotlin-Native**: Built by JetBrains specifically for Kotlin
2. **Type Safety**: DSL provides compile-time query validation
3. **Coroutine Support**: Native suspend functions, async-friendly
4. **Lightweight**: Minimal overhead, simple abstraction over JDBC
5. **Dual API**: Choose between DSL (SQL-like) or DAO (ORM-like)
6. **Active Development**: Regular updates, good community support

### Why Not Ktorm?

- **Smaller Ecosystem**: Less adoption (2k vs 8k stars)
- **Less Official Support**: Independent project vs JetBrains backing
- **Trade-off**: Ktorm has slightly better documentation but less community

### Why Not Spring Data JPA?

- **Heavy Framework**: Brings entire Spring Boot ecosystem
- **Not Kotlin-Idiomatic**: Java-centric APIs with Kotlin extensions
- **Use Case**: Better for large enterprise applications with Spring ecosystem

### Why HikariCP?

- **Performance**: Fastest connection pool available
- **Reliability**: Production-proven, used by major companies
- **Lightweight**: Minimal overhead
- **JVM Standard**: De facto connection pool for JVM applications

## Consequences

### Positive

- ✅ **Kotlin-Idiomatic**: DSL feels natural in Kotlin code
- ✅ **Type Safety**: Compile-time validation prevents SQL errors
- ✅ **Coroutine Support**: Seamless async/await with suspend functions
- ✅ **Lightweight**: Minimal abstraction, predictable performance
- ✅ **Testability**: Easy integration testing with Testcontainers
- ✅ **Flexible**: Choose between DSL and DAO API

### Negative

- ❌ **Less Mature**: Younger than Hibernate/JPA
- ❌ **Smaller Community**: Fewer Stack Overflow answers
- ❌ **Manual Migrations**: Requires Flyway or similar (no built-in migrations)
- ❌ **Learning Curve**: DSL syntax differs from traditional SQL/ORM

### Neutral

- 🔄 **Migration Strategy**: Must choose between Flyway and SchemaUtils
- 🔄 **DAO vs DSL**: Must decide which API to use (recommendation: DSL)
- 🔄 **Connection Pool Tuning**: Must configure HikariCP for production

## Implementation Checklist

- [ ] Add Exposed and HikariCP dependencies
- [ ] Create `DatabaseFactory` and `DatabaseConfig`
- [ ] Define `LampsTable` with DSL
- [ ] Implement `ExposedLampRepository` with suspend functions
- [ ] Configure `application.conf` with database settings
- [ ] Add Flyway for migrations or use SchemaUtils
- [ ] Implement route handlers with coroutines
- [ ] Write integration tests with Testcontainers
- [ ] Update README with database setup instructions
- [ ] Add health check endpoint

## References

- [Exposed Documentation](https://github.com/JetBrains/Exposed/wiki)
- [Exposed GitHub](https://github.com/JetBrains/Exposed)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Ktor Documentation](https://ktor.io/docs/)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [Testcontainers Kotlin](https://kotlin.testcontainers.org/)

## Related ADRs

- [ADR 001: Kotlin Version](001-kotlin-version.md)
- [ADR 002: Web Framework Selection](002-web-framework-ktor.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
