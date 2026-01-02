package org.openapitools.configuration;

import org.openapitools.repository.JpaLampRepository;
import org.openapitools.repository.LampRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for repository selection. When JPA is enabled (i.e., database configuration is
 * provided), the JpaLampRepository will be used. Otherwise, the InMemoryLampRepository will be
 * used.
 */
@Configuration
public class RepositoryConfig {

  /**
   * Creates a primary LampRepository bean that delegates to JpaLampRepository when JPA is
   * available.
   *
   * @param jpaLampRepository the JPA repository implementation
   * @return the primary LampRepository
   */
  @Bean
  @Primary
  @ConditionalOnBean(JpaLampRepository.class)
  public LampRepository primaryLampRepository(final JpaLampRepository jpaLampRepository) {
    return jpaLampRepository;
  }
}
