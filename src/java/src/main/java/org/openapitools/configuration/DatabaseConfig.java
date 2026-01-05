package org.openapitools.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for PostgreSQL with Spring Data JPA. This configuration is activated when
 * database properties are provided. HikariCP is auto-configured by Spring Boot.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.openapitools.repository")
@EnableTransactionManagement
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class DatabaseConfig {
  // Spring Boot auto-configures DataSource, HikariCP, and JPA
  // No manual bean configuration needed
}
