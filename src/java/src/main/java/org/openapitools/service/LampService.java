package org.openapitools.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openapitools.entity.LampEntity;
import org.openapitools.mapper.LampMapper;
import org.openapitools.model.Lamp;
import org.openapitools.repository.LampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Lamp entity operations. Provides business logic and transaction management for
 * CRUD operations on lamps.
 *
 * <p>This service layer follows the pattern specified in ADR 007, providing:
 *
 * <ul>
 *   <li>Declarative transaction management with @Transactional
 *   <li>Soft delete implementation
 *   <li>Pagination support
 *   <li>DTO-to-Entity mapping via LampMapper
 * </ul>
 *
 * <p>Read-only operations are marked with @Transactional(readOnly = true) at the class level for
 * performance optimization. Write operations override with @Transactional.
 */
@Service
@Transactional(readOnly = true)
public class LampService {

  private final LampRepository repository;
  private final LampMapper mapper;

  public LampService(final LampRepository repository, final LampMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Create a new lamp.
   *
   * @param lamp the lamp data to create
   * @return the created lamp with generated ID and timestamps
   */
  @Transactional
  public Lamp create(final Lamp lamp) {
    final LampEntity entity = mapper.toEntity(lamp);
    final LampEntity saved = repository.save(entity);
    return mapper.toModel(saved);
  }

  /**
   * Find a lamp by its ID.
   *
   * @param id the lamp ID
   * @return the lamp if found, null otherwise
   */
  public Lamp findById(final UUID id) {
    return repository.findById(id).map(mapper::toModel).orElse(null);
  }

  /**
   * Find all active lamps with pagination support.
   *
   * @param offset the starting position (0-based)
   * @param limit the maximum number of results to return
   * @return list of lamps for the requested page
   */
  public List<Lamp> findAll(final int offset, final int limit) {
    final Pageable pageable =
        PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.ASC, "createdAt"));

    final Page<LampEntity> page = repository.findAll(pageable);
    return page.getContent().stream().map(mapper::toModel).collect(Collectors.toList());
  }

  /**
   * Find all active lamps without pagination.
   *
   * @return list of all active lamps ordered by creation time
   */
  public List<Lamp> findAllActive() {
    return repository.findAllActive().stream().map(mapper::toModel).collect(Collectors.toList());
  }

  /**
   * Find all lamps with the specified status.
   *
   * @param isOn the status to filter by (true for on, false for off)
   * @return list of lamps with the specified status
   */
  public List<Lamp> findByStatus(final Boolean isOn) {
    return repository.findByStatus(isOn).stream().map(mapper::toModel).collect(Collectors.toList());
  }

  /**
   * Count all active (non-deleted) lamps.
   *
   * @return count of active lamps
   */
  public long countActive() {
    return repository.countActive();
  }

  /**
   * Update an existing lamp.
   *
   * @param id the lamp ID
   * @param lamp the updated lamp data
   * @return the updated lamp if found, null otherwise
   */
  @Transactional
  public Lamp update(final UUID id, final Lamp lamp) {
    return repository
        .findById(id)
        .map(
            entity -> {
              entity.setStatus(lamp.getStatus());
              // updatedAt is automatically set by @UpdateTimestamp
              return mapper.toModel(repository.save(entity));
            })
        .orElse(null);
  }

  /**
   * Soft delete a lamp by setting its deletedAt timestamp.
   *
   * <p>Soft-deleted lamps are automatically filtered from queries by the @Where clause on
   * LampEntity.
   *
   * @param id the lamp ID to delete
   * @return true if the lamp was found and deleted, false otherwise
   */
  @Transactional
  public boolean delete(final UUID id) {
    return repository
        .findById(id)
        .map(
            entity -> {
              entity.setDeletedAt(OffsetDateTime.now());
              repository.save(entity);
              return true;
            })
        .orElse(false);
  }
}
