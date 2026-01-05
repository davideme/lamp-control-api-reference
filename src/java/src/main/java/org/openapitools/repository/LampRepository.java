package org.openapitools.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openapitools.entity.LampEntity;

/**
 * Repository interface for Lamp entities. This interface defines the contract for lamp data
 * operations without being tied to any specific persistence mechanism.
 *
 * <p>Note: This interface is kept minimal to avoid conflicts with JpaRepository methods when using
 * Spring Data JPA implementation.
 */
public interface LampRepository {

  /**
   * Find all lamp entities.
   *
   * @return list of all lamp entities
   */
  List<LampEntity> findAll();

  /**
   * Find a lamp entity by its ID.
   *
   * @param lampId the lamp ID
   * @return optional containing the lamp entity if found
   */
  Optional<LampEntity> findById(UUID lampId);

  /**
   * Save a lamp entity (create or update).
   *
   * @param entity the lamp entity to save
   * @return the saved lamp entity
   */
  LampEntity save(LampEntity entity);

  /**
   * Check if a lamp entity exists by ID.
   *
   * @param lampId the lamp ID
   * @return true if the entity exists
   */
  boolean existsById(UUID lampId);

  /**
   * Delete a lamp entity by ID.
   *
   * @param lampId the lamp ID
   */
  void deleteById(UUID lampId);

  /**
   * Delete all lamp entities.
   *
   * @deprecated Since 1.0.0. This bulk-delete operation is unsafe for production usage and is
   *     intended primarily for testing scenarios. Prefer targeted deletions using {@link
   *     #deleteById(UUID)} or other domain-specific operations instead of removing all records.
   */
  @Deprecated
  void deleteAll();

  /**
   * Count the total number of lamp entities.
   *
   * @return the count of entities
   */
  long count();
}
