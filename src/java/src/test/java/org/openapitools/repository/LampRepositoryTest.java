package org.openapitools.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.impl.InMemoryLampRepository;

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
}
