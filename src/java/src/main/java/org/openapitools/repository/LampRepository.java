package org.openapitools.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openapitools.entity.LampEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface for Lamp entities. This interface defines the contract for lamp data
 * operations without being tied to any specific persistence mechanism.
 *
 * <p>This interface declares both standard CRUD operations and domain-specific query methods. The
 * JPA implementation (JpaLampRepository) will inherit most CRUD methods from JpaRepository and only
 * needs to implement the custom domain methods.
 */
public interface LampRepository {

  /**
   * Save a lamp entity (create or update).
   *
   * @param entity the lamp entity to save
   * @return the saved lamp entity
   */
  LampEntity save(LampEntity entity);

  /**
   * Find a lamp entity by its ID.
   *
   * @param lampId the lamp ID
   * @return optional containing the lamp entity if found
   */
  Optional<LampEntity> findById(UUID lampId);

  /**
   * Find all lamp entities with pagination support.
   *
   * @param pageable pagination information
   * @return page of lamp entities
   */
  Page<LampEntity> findAll(Pageable pageable);

  /**
   * Find all lamp entities.
   *
   * @return list of all lamp entities
   */
  List<LampEntity> findAll();

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
   *     intended primarily for testing scenarios.
   */
  @Deprecated
  void deleteAll();

  /**
   * Count the total number of lamp entities.
   *
   * @return the count of entities
   */
  long count();

  /**
   * Find all lamps with the specified on/off status.
   *
   * @param isOn the status to filter by (true for on, false for off)
   * @return list of lamps with the specified status
   */
  List<LampEntity> findByStatus(Boolean isOn);

  /**
   * Find all active (non-deleted) lamps ordered by creation time.
   *
   * @return list of all active lamps ordered by creation time ascending
   */
  List<LampEntity> findAllActive();

  /**
   * Count all active (non-deleted) lamps.
   *
   * @return count of active lamps
   */
  long countActive();
}
