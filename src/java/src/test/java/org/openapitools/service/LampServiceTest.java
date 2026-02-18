package org.openapitools.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.entity.LampEntity;
import org.openapitools.exception.LampNotFoundException;
import org.openapitools.mapper.LampMapper;
import org.openapitools.model.Lamp;
import org.openapitools.repository.LampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Unit tests for LampService. These tests use mocked dependencies to test the service layer in
 * isolation.
 */
@ExtendWith(MockitoExtension.class)
class LampServiceTest {

  @Mock private LampRepository repository;

  @Mock private LampMapper mapper;

  @InjectMocks private LampService service;

  private UUID testId;
  private LampEntity testEntity;
  private Lamp testLamp;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
    testEntity = new LampEntity(testId, true);
    testLamp = new Lamp();
    testLamp.setId(testId);
    testLamp.setStatus(true);
  }

  @Test
  void shouldCreateLamp() {
    // Arrange
    final Lamp input = new Lamp();
    input.setStatus(true);

    when(mapper.toEntity(input)).thenReturn(testEntity);
    when(repository.save(testEntity)).thenReturn(testEntity);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final Lamp result = service.create(input);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testId);
    assertThat(result.getStatus()).isTrue();
    verify(repository).save(testEntity);
    verify(mapper).toEntity(input);
    verify(mapper).toModel(testEntity);
  }

  @Test
  void shouldFindLampById() {
    // Arrange
    when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final Optional<Lamp> result = service.findById(testId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testId);
    verify(repository).findById(testId);
    verify(mapper).toModel(testEntity);
  }

  @Test
  void shouldReturnEmptyWhenLampNotFound() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act
    final Optional<Lamp> result = service.findById(nonExistentId);

    // Assert
    assertThat(result).isEmpty();
    verify(repository).findById(nonExistentId);
    verify(mapper, never()).toModel(any());
  }

  @Test
  void shouldFindAllWithPagination() {
    // Arrange
    final List<LampEntity> entities = List.of(testEntity);
    final Page<LampEntity> page = new PageImpl<>(entities);

    when(repository.findAll(any(Pageable.class))).thenReturn(page);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final List<Lamp> result = service.findAll(0, 10);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testId);
    verify(repository).findAll(any(Pageable.class));
  }

  @Test
  void shouldFindAllActive() {
    // Arrange
    final List<LampEntity> entities = List.of(testEntity);
    when(repository.findAllActive()).thenReturn(entities);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final List<Lamp> result = service.findAllActive();

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testId);
    verify(repository).findAllActive();
  }

  @Test
  void shouldFindActivePageWithHasMoreAndNextCursor() {
    // Arrange
    final UUID secondId = UUID.randomUUID();
    final LampEntity secondEntity = new LampEntity(secondId, false);
    final Lamp secondLamp = new Lamp(secondId, false);

    final Page<LampEntity> page = new PageImpl<>(List.of(testEntity, secondEntity));
    when(repository.findAll(any(Pageable.class))).thenReturn(page);
    when(repository.countActive()).thenReturn(5L);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);
    when(mapper.toModel(secondEntity)).thenReturn(secondLamp);

    // Act
    final LampService.PagedLampsResult result = service.findAllActivePage(2, 2);

    // Assert
    assertThat(result.data()).hasSize(2);
    assertThat(result.hasMore()).isTrue();
    assertThat(result.nextCursor()).contains("4");

    final var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(pageableCaptor.capture());
    final Pageable captured = pageableCaptor.getValue();
    assertThat(captured.getOffset()).isEqualTo(2);
    assertThat(captured.getPageSize()).isEqualTo(2);
    assertThat(captured.getSort())
        .isEqualTo(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
  }

  @Test
  void shouldFindActivePageTerminalWithoutNextCursor() {
    // Arrange
    final Page<LampEntity> page = new PageImpl<>(List.of(testEntity));
    when(repository.findAll(any(Pageable.class))).thenReturn(page);
    when(repository.countActive()).thenReturn(5L);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final LampService.PagedLampsResult result = service.findAllActivePage(4, 2);

    // Assert
    assertThat(result.data()).hasSize(1);
    assertThat(result.hasMore()).isFalse();
    assertThat(result.nextCursor()).isEmpty();
  }

  @Test
  void shouldFallbackToDefaultPageSizeWhenInvalidPageSizeProvided() {
    // Arrange
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    when(repository.countActive()).thenReturn(0L);

    // Act
    service.findAllActivePage(0, 0);

    // Assert
    final var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(25);
  }

  @Test
  void shouldFindByStatus() {
    // Arrange
    final List<LampEntity> entities = List.of(testEntity);
    when(repository.findByStatus(true)).thenReturn(entities);
    when(mapper.toModel(testEntity)).thenReturn(testLamp);

    // Act
    final List<Lamp> result = service.findByStatus(true);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isTrue();
    verify(repository).findByStatus(true);
  }

  @Test
  void shouldCountActive() {
    // Arrange
    when(repository.countActive()).thenReturn(5L);

    // Act
    final long count = service.countActive();

    // Assert
    assertThat(count).isEqualTo(5L);
    verify(repository).countActive();
  }

  @Test
  void shouldUpdateLamp() {
    // Arrange
    final Lamp updateData = new Lamp();
    updateData.setStatus(false);

    final LampEntity updatedEntity = new LampEntity(testId, false);
    final Lamp updatedLamp = new Lamp();
    updatedLamp.setId(testId);
    updatedLamp.setStatus(false);

    when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
    when(repository.save(testEntity)).thenReturn(updatedEntity);
    when(mapper.toModel(updatedEntity)).thenReturn(updatedLamp);

    // Act
    final Lamp result = service.update(testId, updateData);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testId);
    assertThat(result.getStatus()).isFalse();
    verify(repository).findById(testId);
    verify(repository).save(any(LampEntity.class));
  }

  @Test
  void shouldThrowWhenUpdatingNonExistentLamp() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    final Lamp updateData = new Lamp();
    updateData.setStatus(false);

    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.update(nonExistentId, updateData))
        .isInstanceOf(LampNotFoundException.class);
    verify(repository).findById(nonExistentId);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldSoftDeleteLamp() {
    // Arrange
    when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
    when(repository.save(testEntity)).thenReturn(testEntity);

    // Act
    service.delete(testId);

    // Assert
    verify(repository).findById(testId);
    verify(repository).save(any(LampEntity.class));
  }

  @Test
  void shouldThrowWhenDeletingNonExistentLamp() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.delete(nonExistentId))
        .isInstanceOf(LampNotFoundException.class);
    verify(repository).findById(nonExistentId);
    verify(repository, never()).save(any());
  }
}
