package org.openapitools.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openapitools.entity.LampEntity;

/**
 * In-memory repository interface for Lamp entities. This interface defines the contract for lamp
 * data operations without being tied to any specific persistence mechanism.
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

  /** Delete all lamp entities. */
  void deleteAll();

  /**
   * Count the total number of lamp entities.
   *
   * @return the count of entities
   */
  long count();
}
