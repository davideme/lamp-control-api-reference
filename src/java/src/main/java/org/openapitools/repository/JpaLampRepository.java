package org.openapitools.repository;

import java.util.List;
import java.util.UUID;
import org.openapitools.entity.LampEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA repository interface for Lamp entities. Spring Data JPA will automatically provide an
 * implementation of this interface at runtime, supporting full CRUD operations with PostgreSQL
 * database.
 *
 * <p>This interface extends both JpaRepository and LampRepository to provide JPA-specific methods
 * while maintaining compatibility with the LampRepository interface used by the application.
 *
 * <p>Marked as @Primary to take precedence over InMemoryLampRepository when both are available.
 */
@Repository
@Primary
public interface JpaLampRepository extends JpaRepository<LampEntity, UUID>, LampRepository {

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
