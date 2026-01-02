package org.openapitools.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.LampRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the LampRepository. This implementation uses a ConcurrentHashMap to
 * store lamp entities in memory, providing thread-safe operations suitable for testing and
 * development environments.
 *
 * <p>This implementation is only activated when no other LampRepository bean is available (i.e.,
 * when JPA is not configured).
 */
@Repository
@ConditionalOnMissingBean(name = "jpaLampRepository")
public class InMemoryLampRepository implements LampRepository {

  private final Map<UUID, LampEntity> lamps = new ConcurrentHashMap<>();

  @Override
  public List<LampEntity> findAll() {
    return new ArrayList<>(lamps.values());
  }

  @Override
  public Optional<LampEntity> findById(final UUID lampId) {
    return Optional.ofNullable(lamps.get(lampId));
  }

  @Override
  public LampEntity save(final LampEntity entity) {
    if (entity.getId() == null) {
      entity.setId(UUID.randomUUID());
    }

    // Create a copy to avoid external modifications
    final LampEntity copy = new LampEntity();
    copy.setId(entity.getId());
    copy.setStatus(entity.getStatus());
    copy.setCreatedAt(entity.getCreatedAt());
    copy.setUpdatedAt(entity.getUpdatedAt());

    lamps.put(copy.getId(), copy);
    return copy;
  }

  @Override
  public boolean existsById(final UUID lampId) {
    return lamps.containsKey(lampId);
  }

  @Override
  public void deleteById(final UUID lampId) {
    lamps.remove(lampId);
  }

  @Override
  public void deleteAll() {
    lamps.clear();
  }

  @Override
  public long count() {
    return lamps.size();
  }

  /**
   * Additional utility method for testing - get all stored IDs.
   *
   * @return set of all lamp IDs
   */
  public java.util.Set<UUID> getAllIds() {
    return new java.util.HashSet<>(lamps.keySet());
  }

  /**
   * Additional utility method for testing - check if repository is empty.
   *
   * @return true if no lamps are stored
   */
  public boolean isEmpty() {
    return lamps.isEmpty();
  }
}
