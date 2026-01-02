package org.openapitools.repository;

import java.util.UUID;
import org.openapitools.entity.LampEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository interface for Lamp entities. Spring Data JPA will automatically provide an
 * implementation of this interface at runtime, supporting full CRUD operations with PostgreSQL
 * database.
 *
 * <p>This interface extends both JpaRepository and LampRepository to provide JPA-specific methods
 * while maintaining compatibility with the LampRepository interface used by the application.
 */
@Repository
public interface JpaLampRepository extends JpaRepository<LampEntity, UUID>, LampRepository {}
