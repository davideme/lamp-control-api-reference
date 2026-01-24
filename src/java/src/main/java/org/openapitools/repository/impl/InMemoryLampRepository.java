package org.openapitools.repository.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.openapitools.config.OnNoDatabaseUrlCondition;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.LampRepository;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the LampRepository. This implementation uses a ConcurrentHashMap to
 * store lamp entities in memory, providing thread-safe operations suitable for testing and
 * development environments.
 *
 * <p>This implementation is activated when no database URL is configured (i.e., when
 * spring.datasource.url is empty or missing).
 */
@Repository
@Conditional(OnNoDatabaseUrlCondition.class)
public class InMemoryLampRepository implements LampRepository {

  private final Map<UUID, LampEntity> lamps = new ConcurrentHashMap<>();

  @Override
  public List<LampEntity> findAll() {
    return new ArrayList<>(lamps.values());
  }

  @Override
  public Page<LampEntity> findAll(final Pageable pageable) {
    final List<LampEntity> allLamps = new ArrayList<>(lamps.values());
    final int start = (int) pageable.getOffset();
    final int end = Math.min(start + pageable.getPageSize(), allLamps.size());

    if (start >= allLamps.size()) {
      return new PageImpl<>(List.of(), pageable, allLamps.size());
    }

    final List<LampEntity> pageContent = allLamps.subList(start, end);
    return new PageImpl<>(pageContent, pageable, allLamps.size());
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

    // Check if this is an update or create operation
    final UUID entityId = entity.getId();
    final LampEntity existing = lamps.get(entityId);
    final boolean isUpdate = existing != null;

    // Create a copy to avoid external modifications
    final LampEntity copy = new LampEntity();
    copy.setId(entity.getId());
    copy.setStatus(entity.getStatus());
    copy.setDeletedAt(entity.getDeletedAt());

    // Set timestamps: preserve createdAt on updates, always update updatedAt
    this.setTimestamps(entity, copy, existing, isUpdate);

    lamps.put(copy.getId(), copy);
    return copy;
  }

  private void setTimestamps(
      final LampEntity source,
      final LampEntity target,
      final LampEntity existing,
      final boolean isUpdate) {
    final java.time.OffsetDateTime now = java.time.OffsetDateTime.now();

    // For updates: preserve createdAt from existing entity
    // For creates: use source's createdAt or generate new timestamp
    if (isUpdate) {
      final java.time.OffsetDateTime existingCreatedAt = existing.getCreatedAt();
      target.setCreatedAt(existingCreatedAt);
    } else {
      final java.time.OffsetDateTime sourceCreatedAt = source.getCreatedAt();
      if (sourceCreatedAt != null) {
        target.setCreatedAt(sourceCreatedAt);
      } else {
        target.setCreatedAt(now);
      }
    }

    // Always update updatedAt to current time
    target.setUpdatedAt(now);
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

  @Override
  public List<LampEntity> findByStatus(final Boolean isOn) {
    return lamps.values().stream()
        .filter(lamp -> lamp.getDeletedAt() == null)
        .filter(lamp -> lamp.getStatus().equals(isOn))
        .collect(Collectors.toList());
  }

  @Override
  public List<LampEntity> findAllActive() {
    return lamps.values().stream()
        .filter(lamp -> lamp.getDeletedAt() == null)
        .sorted(Comparator.comparing(LampEntity::getCreatedAt))
        .collect(Collectors.toList());
  }

  @Override
  public long countActive() {
    return lamps.values().stream().filter(lamp -> lamp.getDeletedAt() == null).count();
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
