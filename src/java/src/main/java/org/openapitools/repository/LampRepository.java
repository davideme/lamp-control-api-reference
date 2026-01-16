package org.openapitools.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openapitools.entity.LampEntity;

/**
 * Repository interface for Lamp entities. This interface defines the contract for lamp data
 * operations without being tied to any specific persistence mechanism.
 *
 * <p>This interface only declares domain-specific methods that are NOT already provided by
 * JpaRepository. Common CRUD methods (save, findById, findAll, existsById, deleteById, count) are
 * inherited from JpaRepository when using the JPA implementation.
 *
 * <p>For non-JPA implementations (like InMemoryLampRepository), those common CRUD methods must be
 * implemented directly by the implementation class, even though they're not declared in this
 * interface.
 */
public interface LampRepository {

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
