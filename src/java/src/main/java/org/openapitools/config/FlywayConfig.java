package org.openapitools.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway database migration configuration.
 *
 * <p>This configuration is only activated when:
 *
 * <ul>
 *   <li>A DataSource bean is available (database URL is configured)
 *   <li>spring.flyway.enabled property is set to true
 * </ul>
 *
 * <p>Flyway will automatically run migrations on application startup when this configuration is
 * active.
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
public class FlywayConfig {

  /**
   * Creates and configures Flyway for database migrations.
   *
   * @param dataSource the DataSource to migrate
   * @return configured Flyway instance
   */
  @Bean(initMethod = "migrate")
  public Flyway flyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .validateOnMigrate(true)
        .load();
  }
}
