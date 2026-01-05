package org.openapitools.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.JpaLampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for JpaLampRepository with PostgreSQL using Testcontainers. This test verifies
 * that the repository works correctly with a real PostgreSQL database instance.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaLampRepositoryIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16.1-alpine")
          .withDatabaseName("lampcontrol_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "true");
  }

  @Autowired private JpaLampRepository repository;

  private JpaRepository<LampEntity, UUID> jpaRepo;

  @BeforeEach
  void setUp() {
    // Cast once to avoid repetition in tests
    jpaRepo = (JpaRepository<LampEntity, UUID>) repository;
    jpaRepo.deleteAll();
  }

  @Test
  void shouldSaveAndRetrieveLamp() {
    // Arrange
    final UUID lampId = UUID.randomUUID();
    final LampEntity lamp = new LampEntity(lampId, true);

    // Act
    jpaRepo.save(lamp);
    jpaRepo.flush(); // Force immediate database sync

    // Retrieve from database to get timestamps set by database
    final Optional<LampEntity> retrieved = jpaRepo.findById(lampId);

    // Assert
    assertThat(retrieved).isPresent();
    final LampEntity savedLamp = retrieved.get();
    assertThat(savedLamp.getId()).isEqualTo(lampId);
    assertThat(savedLamp.getStatus()).isTrue();
    assertThat(savedLamp.getCreatedAt()).as("createdAt should be set by database").isNotNull();
    assertThat(savedLamp.getUpdatedAt()).as("updatedAt should be set by database").isNotNull();
  }

  @Test
  void shouldUpdateLampStatus() {
    // Arrange
    final LampEntity lamp = new LampEntity(UUID.randomUUID(), false);
    final LampEntity saved = jpaRepo.save(lamp);

    // Act
    saved.setStatus(true);
    jpaRepo.save(saved);
    final Optional<LampEntity> updated = jpaRepo.findById(saved.getId());

    // Assert
    assertThat(updated).isPresent();
    assertThat(updated.get().getStatus()).isTrue();
  }

  @Test
  void shouldDeleteLamp() {
    // Arrange
    final LampEntity lamp = new LampEntity(UUID.randomUUID(), true);
    final LampEntity saved = jpaRepo.save(lamp);

    // Act
    jpaRepo.deleteById(saved.getId());
    final Optional<LampEntity> deleted = jpaRepo.findById(saved.getId());

    // Assert
    assertThat(deleted).isEmpty();
  }

  @Test
  void shouldCountLamps() {
    // Arrange
    jpaRepo.save(new LampEntity(UUID.randomUUID(), true));
    jpaRepo.save(new LampEntity(UUID.randomUUID(), false));

    // Act
    final long count = jpaRepo.count();

    // Assert
    assertThat(count).isEqualTo(2);
  }

  @Test
  void shouldCheckIfLampExists() {
    // Arrange
    final LampEntity lamp = new LampEntity(UUID.randomUUID(), true);
    final LampEntity saved = jpaRepo.save(lamp);

    // Act
    final boolean exists = jpaRepo.existsById(saved.getId());
    final boolean notExists = jpaRepo.existsById(UUID.randomUUID());

    // Assert
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }
}
