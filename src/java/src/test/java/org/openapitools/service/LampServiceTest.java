package org.openapitools.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
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
import org.openapitools.mapper.LampMapper;
import org.openapitools.model.Lamp;
import org.openapitools.repository.JpaLampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for LampService. These tests use mocked dependencies to test the service layer in
 * isolation.
 */
@ExtendWith(MockitoExtension.class)
class LampServiceTest {

  @Mock private JpaLampRepository repository;

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
    final Lamp result = service.findById(testId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testId);
    verify(repository).findById(testId);
    verify(mapper).toModel(testEntity);
  }

  @Test
  void shouldReturnNullWhenLampNotFound() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act
    final Lamp result = service.findById(nonExistentId);

    // Assert
    assertThat(result).isNull();
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
    verify(repository).save(testEntity);
    verify(testEntity).setStatus(false);
  }

  @Test
  void shouldReturnNullWhenUpdatingNonExistentLamp() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    final Lamp updateData = new Lamp();
    updateData.setStatus(false);

    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act
    final Lamp result = service.update(nonExistentId, updateData);

    // Assert
    assertThat(result).isNull();
    verify(repository).findById(nonExistentId);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldSoftDeleteLamp() {
    // Arrange
    when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
    when(repository.save(testEntity)).thenReturn(testEntity);

    // Act
    final boolean result = service.delete(testId);

    // Assert
    assertThat(result).isTrue();
    verify(repository).findById(testId);
    verify(repository).save(testEntity);
    verify(testEntity).setDeletedAt(any(OffsetDateTime.class));
  }

  @Test
  void shouldReturnFalseWhenDeletingNonExistentLamp() {
    // Arrange
    final UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act
    final boolean result = service.delete(nonExistentId);

    // Assert
    assertThat(result).isFalse();
    verify(repository).findById(nonExistentId);
    verify(repository, never()).save(any());
  }
}
