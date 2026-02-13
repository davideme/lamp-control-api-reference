package org.openapitools.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/** Unit tests for DataSourceConfig bean methods. */
class DataSourceConfigTest {

  @Test
  void hikariConfig_ShouldUseSpringDatasourceUrlAsIs() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "springDatasourceUrl", "postgresql://localhost:5432/lamp");
    setField(config, "databaseUrl", "postgres://ignored:5432/ignored");
    setField(config, "fallbackJdbcUrl", "jdbc:postgresql://localhost:5432/fallback");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("postgresql://localhost:5432/lamp");
    assertThat(result.getUsername()).isEqualTo("user");
    assertThat(result.getPassword()).isEqualTo("pass");
    assertThat(result.getDriverClassName()).isEqualTo("org.postgresql.Driver");
  }

  @Test
  void hikariConfig_ShouldNormalizeDatabaseUrlPostgresqlScheme() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "springDatasourceUrl", "");
    setField(config, "databaseUrl", "postgresql://localhost:5432/lamp");
    setField(config, "fallbackJdbcUrl", "jdbc:postgresql://localhost:5432/fallback");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/lamp");
  }

  @Test
  void hikariConfig_ShouldNormalizeDatabaseUrlPostgresScheme() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "springDatasourceUrl", "");
    setField(config, "databaseUrl", "postgres://localhost:5432/lamp");
    setField(config, "fallbackJdbcUrl", "jdbc:postgresql://localhost:5432/fallback");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/lamp");
  }

  @Test
  void hikariConfig_ShouldKeepJdbcDatabaseUrlUnchanged() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "springDatasourceUrl", "");
    setField(config, "databaseUrl", "jdbc:postgresql://localhost:5432/lamp");
    setField(config, "fallbackJdbcUrl", "jdbc:postgresql://localhost:5432/fallback");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/lamp");
  }

  @Test
  void hikariConfig_ShouldFallbackToSpringDatasourceProperty() throws Exception {
    DataSourceConfig config = new DataSourceConfig();

    setField(config, "springDatasourceUrl", "");
    setField(config, "databaseUrl", "");
    setField(config, "fallbackJdbcUrl", "jdbc:postgresql://localhost:5432/fallback");
    setField(config, "username", "user");
    setField(config, "password", "pass");
    setField(config, "driverClassName", "org.postgresql.Driver");

    HikariConfig result = config.hikariConfig();

    assertThat(result.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/fallback");
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
