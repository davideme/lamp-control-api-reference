package org.openapitools.config;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openapitools.entity.LampEntity;
import org.openapitools.repository.JpaLampRepository;
import org.openapitools.repository.LampRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Configuration class for repository beans. Provides adapter beans that bridge between JPA
 * repositories and domain repository interfaces.
 */
@Configuration
public class RepositoryConfig {

  /**
   * Creates a LampRepository adapter that wraps JpaLampRepository. This adapter allows LampService
   * to depend on the domain interface (LampRepository) while using the JPA implementation behind
   * the scenes.
   *
   * <p>This bean is only created when JpaLampRepository is available (i.e., when database is
   * configured). The adapter is marked as @Primary to take precedence over InMemoryLampRepository.
   *
   * @param jpaRepository the JPA repository implementation
   * @return a LampRepository adapter wrapping the JPA repository
   */
  @Bean
  @Primary
  @ConditionalOnBean(JpaLampRepository.class)
  public LampRepository lampRepository(final JpaLampRepository jpaRepository) {
    return new LampRepository() {
      @Override
      public LampEntity save(final LampEntity entity) {
        return jpaRepository.save(entity);
      }

      @Override
      public Optional<LampEntity> findById(final UUID lampId) {
        return jpaRepository.findById(lampId);
      }

      @Override
      public Page<LampEntity> findAll(final Pageable pageable) {
        return jpaRepository.findAll(pageable);
      }

      @Override
      public List<LampEntity> findAll() {
        return jpaRepository.findAll();
      }

      @Override
      public boolean existsById(final UUID lampId) {
        return jpaRepository.existsById(lampId);
      }

      @Override
      public void deleteById(final UUID lampId) {
        jpaRepository.deleteById(lampId);
      }

      @Override
      public void deleteAll() {
        jpaRepository.deleteAll();
      }

      @Override
      public long count() {
        return jpaRepository.count();
      }

      @Override
      public List<LampEntity> findByStatus(final Boolean isOn) {
        return jpaRepository.findByStatus(isOn);
      }

      @Override
      public List<LampEntity> findAllActive() {
        return jpaRepository.findAllActive();
      }

      @Override
      public long countActive() {
        return jpaRepository.countActive();
      }
    };
  }
}
