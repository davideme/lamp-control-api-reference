package org.openapitools.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Unit tests for FlywayConfig bean method. */
class FlywayConfigTest {

  @Test
  void flyway_ShouldReturnConfiguredFlywayInstance() {
    FlywayConfig config = new FlywayConfig();
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl("jdbc:h2:mem:test");

    Flyway flyway = config.flyway(dataSource);

    assertThat(flyway).isNotNull();
    assertThat(flyway.getConfiguration().getLocations()).hasSize(1);
    assertThat(flyway.getConfiguration().getLocations()[0].getDescriptor())
        .isEqualTo("classpath:db/migration");
    assertThat(flyway.getConfiguration().isBaselineOnMigrate()).isTrue();
    assertThat(flyway.getConfiguration().isValidateOnMigrate()).isTrue();
  }
}
