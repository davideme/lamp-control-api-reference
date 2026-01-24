package org.openapitools.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * DataSource configuration for PostgreSQL database connectivity.
 *
 * <p>This configuration is only activated when spring.datasource.url is set to a non-empty value.
 * When no database URL is configured, the application uses an in-memory repository instead.
 *
 * <p>Uses HikariCP for connection pooling with settings from application.properties (spring
 * .datasource.hikari.*).
 */
@Configuration
@Conditional(OnDatabaseUrlCondition.class)
public class DataSourceConfig {

  @Value("${spring.datasource.url}")
  private String jdbcUrl;

  @Value("${spring.datasource.username:lampuser}")
  private String username;

  @Value("${spring.datasource.password:lamppass}")
  private String password;

  @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
  private String driverClassName;

  /**
   * Creates a HikariCP DataSource bean configured from application.properties.
   *
   * @return configured DataSource using HikariCP connection pooling
   */
  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public DataSource dataSource() {
    // HikariConfig will be populated from spring.datasource.hikari.* properties
    HikariConfig config = new HikariConfig();

    // Set core JDBC properties
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setDriverClassName(driverClassName);

    return new HikariDataSource(config);
  }
}
