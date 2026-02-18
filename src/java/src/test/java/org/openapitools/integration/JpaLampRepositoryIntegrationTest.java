package org.openapitools.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.JpaLampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for JpaLampRepository with PostgreSQL using Testcontainers. This test verifies
 * that the repository works correctly with a real PostgreSQL database instance.
 *
 * <p>This test explicitly imports DataSource and JPA auto-configuration and enables JPA
 * repositories since they are excluded by default in the main application.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackages = "org.openapitools.repository")
@ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
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

  @Autowired private EntityManager entityManager;

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

  @Test
  void shouldSoftDeleteLamp() {
    // Arrange
    final LampEntity lamp = new LampEntity(UUID.randomUUID(), true);
    final LampEntity saved = jpaRepo.save(lamp);
    jpaRepo.flush();

    // Act - soft delete by setting deletedAt
    saved.setDeletedAt(OffsetDateTime.now());
    jpaRepo.save(saved);
    jpaRepo.flush();
    entityManager.clear(); // Clear persistence context to force fresh query

    // Assert - soft deleted lamp should not be found by findById due to @Where clause
    final Optional<LampEntity> retrieved = jpaRepo.findById(saved.getId());
    assertThat(retrieved).isEmpty();
  }

  @Test
  void shouldFilterSoftDeletedFromFindAll() {
    // Arrange
    final LampEntity activeLamp = new LampEntity(UUID.randomUUID(), true);
    final LampEntity deletedLamp = new LampEntity(UUID.randomUUID(), false);

    jpaRepo.save(activeLamp);
    final LampEntity saved = jpaRepo.save(deletedLamp);
    saved.setDeletedAt(OffsetDateTime.now());
    jpaRepo.save(saved);
    jpaRepo.flush();

    // Act
    final List<LampEntity> allLamps = jpaRepo.findAll();

    // Assert - only active lamp should be returned
    assertThat(allLamps).hasSize(1);
    assertThat(allLamps.get(0).getId()).isEqualTo(activeLamp.getId());
  }

  @Test
  void shouldFindByStatus() {
    // Arrange
    final LampEntity onLamp = new LampEntity(UUID.randomUUID(), true);
    final LampEntity offLamp = new LampEntity(UUID.randomUUID(), false);
    jpaRepo.save(onLamp);
    jpaRepo.save(offLamp);

    // Act
    final List<LampEntity> onLamps = repository.findByStatus(true);
    final List<LampEntity> offLamps = repository.findByStatus(false);

    // Assert
    assertThat(onLamps).hasSize(1);
    assertThat(onLamps.get(0).getStatus()).isTrue();
    assertThat(offLamps).hasSize(1);
    assertThat(offLamps.get(0).getStatus()).isFalse();
  }

  @Test
  void shouldFindAllActive() {
    // Arrange
    final LampEntity lamp1 = new LampEntity(UUID.randomUUID(), true);
    final LampEntity lamp2 = new LampEntity(UUID.randomUUID(), false);
    final LampEntity lamp3 = new LampEntity(UUID.randomUUID(), true);

    jpaRepo.save(lamp1);
    jpaRepo.save(lamp2);
    final LampEntity saved = jpaRepo.save(lamp3);

    // Soft delete lamp3
    saved.setDeletedAt(OffsetDateTime.now());
    jpaRepo.save(saved);
    jpaRepo.flush();

    // Act
    final List<LampEntity> activeLamps = repository.findAllActive();

    // Assert - should only return lamp1 and lamp2
    assertThat(activeLamps).hasSize(2);
    assertThat(activeLamps)
        .extracting(LampEntity::getId)
        .containsExactlyInAnyOrder(lamp1.getId(), lamp2.getId());
  }

  @Test
  void shouldCountActive() {
    // Arrange
    jpaRepo.save(new LampEntity(UUID.randomUUID(), true));
    jpaRepo.save(new LampEntity(UUID.randomUUID(), false));
    final LampEntity deletedLamp = new LampEntity(UUID.randomUUID(), true);
    jpaRepo.save(deletedLamp);

    // Soft delete one lamp
    deletedLamp.setDeletedAt(OffsetDateTime.now());
    jpaRepo.save(deletedLamp);
    jpaRepo.flush();

    // Act
    final long activeCount = repository.countActive();

    // Assert - should count only active lamps
    assertThat(activeCount).isEqualTo(2);
  }

  @Test
  void shouldFindAllActiveOrderedByCreatedAt() {
    // Arrange - create lamps with slight delay to ensure different timestamps
    final LampEntity lamp1 = new LampEntity(UUID.randomUUID(), true);
    jpaRepo.save(lamp1);
    jpaRepo.flush();

    final LampEntity lamp2 = new LampEntity(UUID.randomUUID(), false);
    jpaRepo.save(lamp2);
    jpaRepo.flush();

    // Act
    final List<LampEntity> activeLamps = repository.findAllActive();

    // Assert - lamps should be ordered by creation time ascending
    assertThat(activeLamps).hasSize(2);
    assertThat(activeLamps.get(0).getId()).isEqualTo(lamp1.getId());
    assertThat(activeLamps.get(1).getId()).isEqualTo(lamp2.getId());
    assertThat(activeLamps.get(0).getCreatedAt()).isBefore(activeLamps.get(1).getCreatedAt());
  }
}
