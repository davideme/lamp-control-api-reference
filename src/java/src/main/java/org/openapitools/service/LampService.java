package org.openapitools.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openapitools.entity.LampEntity;
import org.openapitools.exception.LampNotFoundException;
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
@RequiredArgsConstructor
public class LampService {

  public record PagedLampsResult(List<Lamp> data, boolean hasMore, Optional<String> nextCursor) {}

  private final LampRepository repository;
  private final LampMapper mapper;

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
   * @return optional containing the lamp if found, empty otherwise
   */
  public Optional<Lamp> findById(final UUID id) {
    return repository.findById(id).map(mapper::toModel);
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
    return page.getContent().stream().map(mapper::toModel).toList();
  }

  /**
   * Find all active lamps without pagination.
   *
   * @return list of all active lamps ordered by creation time
   */
  public List<Lamp> findAllActive() {
    return repository.findAllActive().stream().map(mapper::toModel).toList();
  }

  /**
   * Find a page of active lamps using offset-based cursor pagination.
   *
   * @param offset starting position in the active lamp list (0-based)
   * @param pageSize maximum number of lamps to return
   * @return paged lamps and pagination metadata
   */
  public PagedLampsResult findAllActivePage(final int offset, final int pageSize) {
    final int safeOffset = Math.max(offset, 0);
    final int safePageSize = pageSize > 0 ? pageSize : 25;
    final Pageable pageable =
        new OffsetBasedPageRequest(
            safeOffset, safePageSize, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));

    final Page<LampEntity> page = repository.findAll(pageable);
    final List<Lamp> data = page.getContent().stream().map(mapper::toModel).toList();
    final long totalActive = repository.countActive();
    final boolean hasMore = safeOffset + data.size() < totalActive;
    final Optional<String> nextCursor =
        hasMore ? Optional.of(Integer.toString(safeOffset + data.size())) : Optional.empty();

    return new PagedLampsResult(data, hasMore, nextCursor);
  }

  /**
   * Find all lamps with the specified status.
   *
   * @param isOn the status to filter by (true for on, false for off)
   * @return list of lamps with the specified status
   */
  public List<Lamp> findByStatus(final Boolean isOn) {
    return repository.findByStatus(isOn).stream().map(mapper::toModel).toList();
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
   * @return the updated lamp
   * @throws LampNotFoundException if no lamp exists with the given ID
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
        .orElseThrow(() -> new LampNotFoundException(id));
  }

  /**
   * Soft delete a lamp by setting its deletedAt timestamp.
   *
   * <p>Soft-deleted lamps are automatically filtered from queries by the @Where clause on
   * LampEntity.
   *
   * @param id the lamp ID to delete
   * @throws LampNotFoundException if no lamp exists with the given ID
   */
  @Transactional
  public void delete(final UUID id) {
    final LampEntity entity =
        repository.findById(id).orElseThrow(() -> new LampNotFoundException(id));
    entity.setDeletedAt(OffsetDateTime.now());
    repository.save(entity);
  }

  private static final class OffsetBasedPageRequest implements Pageable {
    private final int offset;
    private final int pageSize;
    private final Sort sort;

    private OffsetBasedPageRequest(final int offset, final int pageSize, final Sort sort) {
      this.offset = offset;
      this.pageSize = pageSize;
      this.sort = sort;
    }

    @Override
    public int getPageNumber() {
      return offset / pageSize;
    }

    @Override
    public int getPageSize() {
      return pageSize;
    }

    @Override
    public long getOffset() {
      return offset;
    }

    @Override
    public Sort getSort() {
      return sort;
    }

    @Override
    public Pageable next() {
      return new OffsetBasedPageRequest(offset + pageSize, pageSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
      if (offset < pageSize) {
        return first();
      }
      return new OffsetBasedPageRequest(offset - pageSize, pageSize, sort);
    }

    @Override
    public Pageable first() {
      return new OffsetBasedPageRequest(0, pageSize, sort);
    }

    @Override
    public Pageable withPage(final int pageNumber) {
      return new OffsetBasedPageRequest(pageNumber * pageSize, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
      return offset > 0;
    }
  }
}
