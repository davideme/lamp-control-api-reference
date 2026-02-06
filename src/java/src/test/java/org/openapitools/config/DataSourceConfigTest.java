package org.openapitools.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/** Unit tests for DataSourceConfig bean methods. */
class DataSourceConfigTest {

  @Test
  void hikariConfig_ShouldReturnConfiguredHikariConfig() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "jdbcUrl", "jdbc:postgresql://localhost:5432/lamp");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/lamp");
    assertThat(result.getUsername()).isEqualTo("user");
    assertThat(result.getPassword()).isEqualTo("pass");
    assertThat(result.getDriverClassName()).isEqualTo("org.postgresql.Driver");
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
