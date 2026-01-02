# ADR 007: PostgreSQL Storage with Spring Data JPA

## Status

Accepted

## Context

The Java implementation of the Lamp Control API currently uses an in-memory repository for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with proper transaction management, connection pooling, and enterprise-grade reliability.

### Current State

- **Framework**: Spring Boot 3.1.3 with Spring Web
- **Architecture**: `LampRepository` interface with manual in-memory implementation
- **Storage**: HashMap-based repository with manual UUID generation
- **Dependencies**: Spring Boot Starter Web, Validation, Testcontainers

### Requirements

1. **Enterprise-Grade Persistence**: ACID transactions, connection pooling, reliability
2. **Spring Integration**: Seamless integration with Spring Boot ecosystem
3. **Type Safety**: Compile-time validation with JPA entities
4. **Developer Productivity**: Minimal boilerplate, automatic repository implementation
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with Testcontainers

### Technology Landscape (2025-2026)

**Spring Data JPA + Hibernate**
- Industry standard (85% of Spring Boot applications)
- Automatic repository implementation from interfaces
- Query derivation from method names
- Declarative transaction management
- Audit support, pagination, specifications
- Spring Boot auto-configuration

**HikariCP**
- Fastest JDBC connection pool (benchmark leader)
- Default in Spring Boot 2.0+
- Zero-overhead design (~130KB)
- Production-proven reliability

**jOOQ**
- SQL-first approach, type-safe query building
- Code generation from database schema
- Excellent for complex queries
- Commercial licensing for some databases

**MyBatis**
- XML/annotation-based SQL mapping
- Full SQL control
- More boilerplate than JPA
- Legacy approach

## Decision

We will implement **Spring Data JPA with Hibernate and HikariCP** as the PostgreSQL data access layer for the Java Lamp Control API implementation.

### Architecture

```
Controllers → LampRepository (Interface) → Spring Data JPA → Hibernate → HikariCP → PostgreSQL
```

### Core Components

#### 1. **Entity Model**

```java
package com.lampcontrol.api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lamps", indexes = {
    @Index(name = "idx_lamps_is_on", columnList = "is_on"),
    @Index(name = "idx_lamps_created_at", columnList = "created_at"),
    @Index(name = "idx_lamps_deleted_at", columnList = "deleted_at")
})
@Where(clause = "deleted_at IS NULL")
public class LampEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "is_on", nullable = false)
    private boolean isOn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // Constructors
    public LampEntity() {
        this.id = UUID.randomUUID();
    }

    public LampEntity(UUID id, boolean isOn) {
        this.id = id;
        this.isOn = isOn;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
```

#### 2. **Repository Interface**

```java
package com.lampcontrol.api.repository;

import com.lampcontrol.api.entity.LampEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LampRepository extends JpaRepository<LampEntity, UUID> {

    // Derived query methods (Spring Data generates implementation)
    List<LampEntity> findByIsOn(boolean isOn);

    // Pagination support
    Page<LampEntity> findAll(Pageable pageable);

    // Custom query with JPQL
    @Query("SELECT l FROM LampEntity l WHERE l.deletedAt IS NULL ORDER BY l.createdAt ASC")
    List<LampEntity> findAllActive();

    // Count active lamps
    @Query("SELECT COUNT(l) FROM LampEntity l WHERE l.deletedAt IS NULL")
    long countActive();
}
```

#### 3. **Service Layer**

```java
package com.lampcontrol.api.service;

import com.lampcontrol.api.entity.LampEntity;
import com.lampcontrol.api.model.Lamp;
import com.lampcontrol.api.repository.LampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LampService {

    private final LampRepository repository;

    public LampService(LampRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Lamp create(Lamp lamp) {
        LampEntity entity = new LampEntity(lamp.getId(), lamp.isOn());
        LampEntity saved = repository.save(entity);
        return mapToDto(saved);
    }

    public Lamp findById(UUID id) {
        return repository.findById(id)
            .map(this::mapToDto)
            .orElse(null);
    }

    public List<Lamp> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.ASC, "createdAt")
        );
        
        Page<LampEntity> page = repository.findAll(pageable);
        return page.getContent().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public Lamp update(UUID id, Lamp lamp) {
        return repository.findById(id)
            .map(entity -> {
                entity.setOn(lamp.isOn());
                // updatedAt is automatically set by @UpdateTimestamp
                return mapToDto(repository.save(entity));
            })
            .orElse(null);
    }

    @Transactional
    public boolean delete(UUID id) {
        return repository.findById(id)
            .map(entity -> {
                entity.setDeletedAt(OffsetDateTime.now());
                repository.save(entity);
                return true;
            })
            .orElse(false);
    }

    private Lamp mapToDto(LampEntity entity) {
        Lamp lamp = new Lamp();
        lamp.setId(entity.getId());
        lamp.setOn(entity.isOn());
        lamp.setCreatedAt(entity.getCreatedAt());
        lamp.setUpdatedAt(entity.getUpdatedAt());
        return lamp;
    }
}
```

#### 4. **Configuration**

```java
package com.lampcontrol.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.lampcontrol.api.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public DataSource dataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }
}
```

### Application Configuration

#### **application.properties** (Development)

```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/lampcontrol
spring.datasource.username=lampuser
spring.datasource.password=lamppass
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=LampControlHikariCP

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.zaxxer.hikari=INFO
```

#### **application-prod.properties** (Production)

```properties
# Database Connection (use environment variables)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

# HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.generate_statistics=false

# Logging
logging.level.org.hibernate.SQL=WARN
logging.level.com.zaxxer.hikari=INFO
```

#### **Environment Variables**

```bash
DATABASE_URL=jdbc:postgresql://db.production.com:5432/lampcontrol?ssl=true
DB_USER=lampuser
DB_PASSWORD=secure_password
SPRING_PROFILES_ACTIVE=prod
```

### Dependencies

#### **pom.xml**

```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA (includes Hibernate) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL JDBC Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- HikariCP (included by default in spring-boot-starter-data-jpa) -->
    <!-- Explicit dependency for version control -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>

    <!-- Flyway for migrations -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Testcontainers for integration tests -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Migration Strategy

#### **Flyway Migrations** (Recommended)

```bash
# Create migrations directory
mkdir -p src/main/resources/db/migration

# Create initial migration from existing schema
cat << 'EOF' > src/main/resources/db/migration/V1__Initial_schema.sql
-- PostgreSQL Schema for Lamp Control API
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE lamps IS 'Stores lamp entities and their current status';
COMMENT ON COLUMN lamps.id IS 'Unique identifier for the lamp';
COMMENT ON COLUMN lamps.is_on IS 'Current status of the lamp (true = ON, false = OFF)';
COMMENT ON COLUMN lamps.created_at IS 'Timestamp when the lamp was created';
COMMENT ON COLUMN lamps.updated_at IS 'Timestamp when the lamp was last updated';
COMMENT ON COLUMN lamps.deleted_at IS 'Timestamp when the lamp was soft deleted, NULL if active';

CREATE INDEX IF NOT EXISTS idx_lamps_is_on ON lamps (is_on);
CREATE INDEX IF NOT EXISTS idx_lamps_created_at ON lamps (created_at);
CREATE INDEX IF NOT EXISTS idx_lamps_deleted_at ON lamps (deleted_at);

CREATE OR REPLACE FUNCTION UPDATE_UPDATED_AT_COLUMN()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_lamps_updated_at
BEFORE UPDATE ON lamps
FOR EACH ROW
EXECUTE FUNCTION UPDATE_UPDATED_AT_COLUMN();
EOF
```

#### **Flyway Configuration**

```properties
# application.properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
```

**Flyway runs automatically on application startup!**

### Testing Strategy

#### **Integration Test with Testcontainers**

```java
package com.lampcontrol.api.repository;

import com.lampcontrol.api.entity.LampEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LampRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("lampcontrol_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LampRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveLamp() {
        // Arrange
        LampEntity lamp = new LampEntity(UUID.randomUUID(), true);

        // Act
        LampEntity saved = repository.save(lamp);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<LampEntity> retrieved = repository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().isOn()).isTrue();
        assertThat(retrieved.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindAllActiveLamps() {
        // Arrange
        LampEntity lamp1 = new LampEntity(UUID.randomUUID(), true);
        LampEntity lamp2 = new LampEntity(UUID.randomUUID(), false);
        repository.saveAll(List.of(lamp1, lamp2));
        entityManager.flush();

        // Act
        List<LampEntity> lamps = repository.findAll();

        // Assert
        assertThat(lamps).hasSize(2);
    }

    @Test
    void shouldSoftDeleteLamp() {
        // Arrange
        LampEntity lamp = new LampEntity(UUID.randomUUID(), true);
        LampEntity saved = repository.save(lamp);
        entityManager.flush();

        // Act
        saved.setDeletedAt(java.time.OffsetDateTime.now());
        repository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Assert - soft deleted lamps are filtered by @Where clause
        Optional<LampEntity> retrieved = repository.findById(saved.getId());
        assertThat(retrieved).isEmpty();
    }
}
```

#### **Service Layer Test**

```java
package com.lampcontrol.api.service;

import com.lampcontrol.api.entity.LampEntity;
import com.lampcontrol.api.model.Lamp;
import com.lampcontrol.api.repository.LampRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LampServiceTest {

    @Mock
    private LampRepository repository;

    @InjectMocks
    private LampService service;

    @Test
    void shouldCreateLamp() {
        // Arrange
        UUID id = UUID.randomUUID();
        Lamp input = new Lamp();
        input.setId(id);
        input.setOn(true);

        LampEntity entity = new LampEntity(id, true);
        when(repository.save(any(LampEntity.class))).thenReturn(entity);

        // Act
        Lamp result = service.create(input);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.isOn()).isTrue();
        verify(repository).save(any(LampEntity.class));
    }

    @Test
    void shouldReturnNullWhenLampNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act
        Lamp result = service.findById(id);

        // Assert
        assertThat(result).isNull();
    }
}
```

### Performance Optimizations

#### **1. Query Optimization**

```java
// Use @EntityGraph for eager loading
public interface LampRepository extends JpaRepository<LampEntity, UUID> {
    @EntityGraph(attributePaths = {"relatedEntity"})
    Optional<LampEntity> findWithRelationsById(UUID id);
}

// Use projections for partial data
public interface LampSummary {
    UUID getId();
    boolean isOn();
}

List<LampSummary> findAllProjectedBy();
```

#### **2. Batch Operations**

```java
// Batch inserts
@Transactional
public void createBatch(List<Lamp> lamps) {
    List<LampEntity> entities = lamps.stream()
        .map(this::mapToEntity)
        .collect(Collectors.toList());
    
    repository.saveAll(entities);
}

// Configure batch size in application.properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

#### **3. Caching**

```java
// Enable second-level cache
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LampEntity {
    // ...
}

// application.properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

## Rationale

### Why Spring Data JPA?

1. **Industry Standard**: 85% of Spring Boot applications use Spring Data JPA
2. **Productivity**: Automatic repository implementation, query derivation
3. **Spring Ecosystem**: Seamless integration with Spring Boot, Security, Actuator
4. **Declarative**: Annotations for transactions, caching, auditing
5. **Type Safety**: Compile-time validation, refactoring support
6. **Testing**: Excellent test support with `@DataJpaTest`

### Why Hibernate?

1. **Default JPA Implementation**: Included in Spring Boot starter
2. **Mature**: 20+ years of development, battle-tested
3. **Feature-Rich**: Caching, lazy loading, batch operations
4. **Performance**: Query optimization, statement batching

### Why HikariCP?

1. **Fastest**: Outperforms all other connection pools in benchmarks
2. **Default**: Included in Spring Boot, zero configuration needed
3. **Reliable**: Production-proven, used by major companies
4. **Lightweight**: Minimal overhead (~130KB)

### Why Not jOOQ?

- **Use Case**: Better for complex SQL, database-first approaches
- **Trade-off**: More boilerplate, commercial licensing
- **Decision**: Spring Data JPA better for entity-centric APIs

### Why Not MyBatis?

- **Legacy**: Older approach, XML-heavy configuration
- **Boilerplate**: More manual mapping than JPA
- **Decision**: Spring Data JPA is more modern

## Consequences

### Positive

- ✅ **Zero Boilerplate**: Automatic repository implementation
- ✅ **Spring Integration**: First-class Spring Boot support
- ✅ **Type Safety**: Compile-time validation with entities
- ✅ **Productivity**: Query derivation, pagination, auditing
- ✅ **Testing**: Excellent Testcontainers support
- ✅ **Monitoring**: Built-in metrics, health checks

### Negative

- ❌ **Learning Curve**: JPA/Hibernate abstractions can be complex
- ❌ **Performance**: 10-15% slower than raw JDBC (acceptable trade-off)
- ❌ **N+1 Queries**: Lazy loading can cause performance issues
- ❌ **Magic**: Auto-configuration can hide configuration issues

### Neutral

- 🔄 **Migration Management**: Flyway runs automatically (can be disabled)
- 🔄 **Entity Design**: Must design entities carefully for performance
- 🔄 **Query Tuning**: Complex queries may need optimization

## Implementation Checklist

- [ ] Add Spring Data JPA and PostgreSQL dependencies
- [ ] Create `LampEntity` with JPA annotations
- [ ] Create `LampRepository` interface extending `JpaRepository`
- [ ] Implement `LampService` with transaction management
- [ ] Configure `application.properties` with database settings
- [ ] Add Flyway migration with initial schema
- [ ] Configure HikariCP connection pool
- [ ] Add health check actuator endpoint
- [ ] Write integration tests with Testcontainers
- [ ] Update README with database setup instructions
- [ ] Document environment variables for production

## References

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [Testcontainers Documentation](https://testcontainers.com/)

## Related ADRs

- [ADR 001: Java Version Selection](001-java-version-selection.md)
- [ADR 002: Build Tool Selection](002-build-tool-maven.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
