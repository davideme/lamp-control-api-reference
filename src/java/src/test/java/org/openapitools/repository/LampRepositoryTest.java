package org.openapitools.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.impl.InMemoryLampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for the in-memory lamp repository implementation. These tests verify the basic CRUD
 * operations without requiring a database or Spring context.
 */
class LampRepositoryTest {

  private LampRepository lampRepository;

  @BeforeEach
  void setUp() {
    lampRepository = new InMemoryLampRepository();
  }

  @Test
  void findAll_ShouldReturnAllLamps() {
    // Given
    LampEntity lamp1 = new LampEntity(true);
    LampEntity lamp2 = new LampEntity(false);
    lampRepository.save(lamp1);
    lampRepository.save(lamp2);

    // When
    List<LampEntity> result = lampRepository.findAll();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(LampEntity::getStatus).containsExactlyInAnyOrder(true, false);
  }

  @Test
  void findById_WithExistingId_ShouldReturnLamp() {
    // Given
    LampEntity lamp = new LampEntity(true);
    LampEntity savedLamp = lampRepository.save(lamp);

    // When
    Optional<LampEntity> result = lampRepository.findById(savedLamp.getId());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isTrue();
    assertThat(result.get().getId()).isEqualTo(savedLamp.getId());
  }

  @Test
  void findById_WithNonExistingId_ShouldReturnEmpty() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    Optional<LampEntity> result = lampRepository.findById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void save_ShouldPersistNewLamp() {
    // Given
    LampEntity lamp = new LampEntity(true);

    // When
    LampEntity savedLamp = lampRepository.save(lamp);

    // Then
    assertThat(savedLamp.getId()).isNotNull();
    assertThat(savedLamp.getStatus()).isTrue();

    // Verify persistence
    Optional<LampEntity> foundLamp = lampRepository.findById(savedLamp.getId());
    assertThat(foundLamp).isPresent();
    assertThat(foundLamp.get().getStatus()).isTrue();
  }

  @Test
  void save_ShouldUpdateExistingLamp() {
    // Given
    LampEntity lamp = new LampEntity(true);
    LampEntity savedLamp = lampRepository.save(lamp);

    // When
    savedLamp.setStatus(false);
    LampEntity updatedLamp = lampRepository.save(savedLamp);

    // Then
    assertThat(updatedLamp.getId()).isEqualTo(savedLamp.getId());
    assertThat(updatedLamp.getStatus()).isFalse();

    // Verify persistence
    Optional<LampEntity> foundLamp = lampRepository.findById(savedLamp.getId());
    assertThat(foundLamp).isPresent();
    assertThat(foundLamp.get().getStatus()).isFalse();
  }

  @Test
  void deleteById_WithExistingId_ShouldRemoveLamp() {
    // Given
    LampEntity lamp = new LampEntity(true);
    LampEntity savedLamp = lampRepository.save(lamp);
    UUID lampId = savedLamp.getId();

    // When
    lampRepository.deleteById(lampId);

    // Then
    Optional<LampEntity> foundLamp = lampRepository.findById(lampId);
    assertThat(foundLamp).isEmpty();
    assertThat(lampRepository.existsById(lampId)).isFalse();
  }

  @Test
  void existsById_WithExistingId_ShouldReturnTrue() {
    // Given
    LampEntity lamp = new LampEntity(true);
    LampEntity savedLamp = lampRepository.save(lamp);

    // When
    boolean exists = lampRepository.existsById(savedLamp.getId());

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  void existsById_WithNonExistingId_ShouldReturnFalse() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    boolean exists = lampRepository.existsById(nonExistentId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  void deleteAll_ShouldRemoveAllLamps() {
    // Given
    LampEntity lamp1 = new LampEntity(true);
    LampEntity lamp2 = new LampEntity(false);
    lampRepository.save(lamp1);
    lampRepository.save(lamp2);

    // When
    lampRepository.deleteAll();

    // Then
    List<LampEntity> result = lampRepository.findAll();
    assertThat(result).isEmpty();
    assertThat(lampRepository.count()).isZero();
  }

  @Test
  void count_ShouldReturnCorrectNumber() {
    // Given
    assertThat(lampRepository.count()).isZero();

    // When
    lampRepository.save(new LampEntity(true));
    lampRepository.save(new LampEntity(false));

    // Then
    assertThat(lampRepository.count()).isEqualTo(2);
  }

  @Test
  void getAllIds_ShouldReturnAllLampIds() {
    // Given
    InMemoryLampRepository inMemoryRepo = (InMemoryLampRepository) lampRepository;
    LampEntity lamp1 = new LampEntity(true);
    LampEntity lamp2 = new LampEntity(false);
    LampEntity savedLamp1 = lampRepository.save(lamp1);
    LampEntity savedLamp2 = lampRepository.save(lamp2);

    // When
    java.util.Set<UUID> ids = inMemoryRepo.getAllIds();

    // Then
    assertThat(ids).hasSize(2);
    assertThat(ids).containsExactlyInAnyOrder(savedLamp1.getId(), savedLamp2.getId());
  }

  @Test
  void getAllIds_WithEmptyRepository_ShouldReturnEmptySet() {
    // Given
    InMemoryLampRepository inMemoryRepo = (InMemoryLampRepository) lampRepository;

    // When
    java.util.Set<UUID> ids = inMemoryRepo.getAllIds();

    // Then
    assertThat(ids).isEmpty();
  }

  @Test
  void isEmpty_WithEmptyRepository_ShouldReturnTrue() {
    // Given
    InMemoryLampRepository inMemoryRepo = (InMemoryLampRepository) lampRepository;

    // When
    boolean result = inMemoryRepo.isEmpty();

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void isEmpty_WithLamps_ShouldReturnFalse() {
    // Given
    InMemoryLampRepository inMemoryRepo = (InMemoryLampRepository) lampRepository;
    lampRepository.save(new LampEntity(true));

    // When
    boolean result = inMemoryRepo.isEmpty();

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void isEmpty_AfterClearingRepository_ShouldReturnTrue() {
    // Given
    InMemoryLampRepository inMemoryRepo = (InMemoryLampRepository) lampRepository;
    lampRepository.save(new LampEntity(true));
    lampRepository.save(new LampEntity(false));

    // When
    lampRepository.deleteAll();
    boolean result = inMemoryRepo.isEmpty();

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void findByStatus_ShouldReturnOnlyMatchingActiveLamps() {
    // Given
    LampEntity onLamp = new LampEntity(true);
    LampEntity offLamp = new LampEntity(false);
    LampEntity deletedOnLamp = new LampEntity(true);
    lampRepository.save(onLamp);
    lampRepository.save(offLamp);
    LampEntity savedDeleted = lampRepository.save(deletedOnLamp);
    savedDeleted.setDeletedAt(OffsetDateTime.now());
    lampRepository.save(savedDeleted);

    // When
    List<LampEntity> onLamps = lampRepository.findByStatus(true);

    // Then - should exclude soft-deleted lamp
    assertThat(onLamps).hasSize(1);
    assertThat(onLamps.get(0).getStatus()).isTrue();
  }

  @Test
  void findByStatus_WithFalse_ShouldReturnOffLamps() {
    // Given
    lampRepository.save(new LampEntity(true));
    lampRepository.save(new LampEntity(false));
    lampRepository.save(new LampEntity(false));

    // When
    List<LampEntity> offLamps = lampRepository.findByStatus(false);

    // Then
    assertThat(offLamps).hasSize(2);
    assertThat(offLamps).allMatch(lamp -> !lamp.getStatus());
  }

  @Test
  void findAllActive_ShouldExcludeSoftDeletedLamps() {
    // Given
    lampRepository.save(new LampEntity(true));
    lampRepository.save(new LampEntity(false));
    LampEntity deletedLamp = lampRepository.save(new LampEntity(true));
    deletedLamp.setDeletedAt(OffsetDateTime.now());
    lampRepository.save(deletedLamp);

    // When
    List<LampEntity> activeLamps = lampRepository.findAllActive();

    // Then
    assertThat(activeLamps).hasSize(2);
    assertThat(activeLamps).allMatch(lamp -> lamp.getDeletedAt() == null);
  }

  @Test
  void findAllActive_ShouldReturnSortedByCreatedAt() {
    // Given
    LampEntity lamp1 = lampRepository.save(new LampEntity(true));
    LampEntity lamp2 = lampRepository.save(new LampEntity(false));

    // When
    List<LampEntity> activeLamps = lampRepository.findAllActive();

    // Then
    assertThat(activeLamps).hasSize(2);
    assertThat(activeLamps.get(0).getCreatedAt())
        .isBeforeOrEqualTo(activeLamps.get(1).getCreatedAt());
  }

  @Test
  void countActive_ShouldExcludeSoftDeletedLamps() {
    // Given
    lampRepository.save(new LampEntity(true));
    lampRepository.save(new LampEntity(false));
    LampEntity deletedLamp = lampRepository.save(new LampEntity(true));
    deletedLamp.setDeletedAt(OffsetDateTime.now());
    lampRepository.save(deletedLamp);

    // When
    long activeCount = lampRepository.countActive();

    // Then
    assertThat(activeCount).isEqualTo(2);
  }

  @Test
  void countActive_WithNoLamps_ShouldReturnZero() {
    assertThat(lampRepository.countActive()).isZero();
  }

  @Test
  void findAllPaged_ShouldReturnCorrectPage() {
    // Given
    for (int i = 0; i < 5; i++) {
      lampRepository.save(new LampEntity(i % 2 == 0));
    }
    LampEntity deletedLamp = lampRepository.save(new LampEntity(true));
    deletedLamp.setDeletedAt(OffsetDateTime.now());
    lampRepository.save(deletedLamp);

    // When
    Page<LampEntity> page = lampRepository.findAll(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getContent()).allMatch(lamp -> lamp.getDeletedAt() == null);
  }

  @Test
  void findAllPaged_BeyondData_ShouldReturnEmptyPage() {
    // Given
    lampRepository.save(new LampEntity(true));

    // When
    Page<LampEntity> page = lampRepository.findAll(PageRequest.of(5, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  void findAllPaged_SecondPage_ShouldReturnRemainder() {
    // Given
    for (int i = 0; i < 5; i++) {
      lampRepository.save(new LampEntity(true));
    }

    // When
    Page<LampEntity> page = lampRepository.findAll(PageRequest.of(1, 3));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
  }

  @Test
  void findAllPaged_ShouldReturnDeterministicOrderByCreatedAtThenId() {
    // Given
    final OffsetDateTime baseTime = OffsetDateTime.now();
    final LampEntity firstByIdAtSameTime =
        new LampEntity(UUID.fromString("00000000-0000-0000-0000-000000000001"), true);
    firstByIdAtSameTime.setCreatedAt(baseTime.minusMinutes(2));
    lampRepository.save(firstByIdAtSameTime);

    final LampEntity secondByIdAtSameTime =
        new LampEntity(UUID.fromString("00000000-0000-0000-0000-000000000002"), true);
    secondByIdAtSameTime.setCreatedAt(baseTime.minusMinutes(2));
    lampRepository.save(secondByIdAtSameTime);

    final LampEntity latest =
        new LampEntity(UUID.fromString("00000000-0000-0000-0000-000000000003"), true);
    latest.setCreatedAt(baseTime.minusMinutes(1));
    lampRepository.save(latest);

    // When
    final Page<LampEntity> page = lampRepository.findAll(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent())
        .extracting(LampEntity::getId)
        .containsExactly(firstByIdAtSameTime.getId(), secondByIdAtSameTime.getId(), latest.getId());
  }
}
