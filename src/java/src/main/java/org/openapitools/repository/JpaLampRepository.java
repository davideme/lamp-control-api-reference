package org.openapitools.repository;

import java.util.List;
import java.util.UUID;
import org.openapitools.config.OnDatabaseUrlCondition;
import org.openapitools.entity.LampEntity;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA repository interface for Lamp entities. Spring Data JPA will automatically provide an
 * implementation of this interface at runtime, supporting full CRUD operations with PostgreSQL
 * database.
 *
 * <p>This interface extends JpaRepository (which provides CRUD operations) and implements the
 * custom domain-specific query methods. It does NOT extend LampRepository to avoid method ambiguity
 * issues between JpaRepository and LampRepository method signatures.
 *
 * <p>This repository is only activated when a database URL is configured via the
 * spring.datasource.url property. When no database is configured, the InMemoryLampRepository is
 * used instead.
 *
 * <p>Marked as @Primary to take precedence over InMemoryLampRepository when both are available.
 */
@Repository
@Primary
@Conditional(OnDatabaseUrlCondition.class)
public interface JpaLampRepository extends JpaRepository<LampEntity, UUID> {

  /**
   * Find all lamps with the specified on/off status.
   *
   * <p>This method uses Spring Data JPA's query derivation from method name to automatically
   * generate the query.
   *
   * @param isOn the status to filter by (true for on, false for off)
   * @return list of lamps with the specified status
   */
  List<LampEntity> findByStatus(Boolean isOn);

  /**
   * Find all active (non-deleted) lamps ordered by creation time.
   *
   * <p>Uses explicit JPQL query to ensure only active lamps are returned, sorted by creation time.
   *
   * @return list of all active lamps ordered by creation time ascending
   */
  @Query("SELECT l FROM LampEntity l WHERE l.deletedAt IS NULL ORDER BY l.createdAt ASC")
  List<LampEntity> findAllActive();

  /**
   * Count all active (non-deleted) lamps.
   *
   * <p>Uses explicit JPQL query to count only lamps that have not been soft-deleted.
   *
   * @return count of active lamps
   */
  @Query("SELECT COUNT(l) FROM LampEntity l WHERE l.deletedAt IS NULL")
  long countActive();
}
