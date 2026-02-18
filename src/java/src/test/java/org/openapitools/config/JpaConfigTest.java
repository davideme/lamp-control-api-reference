package org.openapitools.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/** Unit tests for JpaConfig bean methods. */
class JpaConfigTest {

  @Test
  void entityManagerFactory_ShouldReturnConfiguredBean() throws Exception {
    JpaConfig config = new JpaConfig();
    setField(config, "ddlAuto", "validate");
    setField(config, "showSql", false);
    setField(config, "formatSql", true);
    setField(config, "dialect", "org.hibernate.dialect.PostgreSQLDialect");
    setField(config, "timeZone", "UTC");

    DataSource dataSource = new DriverManagerDataSource();

    LocalContainerEntityManagerFactoryBean em = config.entityManagerFactory(dataSource);

    assertThat(em).isNotNull();
    assertThat(em.getDataSource()).isEqualTo(dataSource);
    assertThat(em.getJpaPropertyMap()).containsEntry("hibernate.hbm2ddl.auto", "validate");
    assertThat(em.getJpaPropertyMap())
        .containsEntry("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    assertThat(em.getJpaPropertyMap()).containsEntry("hibernate.jdbc.time_zone", "UTC");
    assertThat(em.getJpaPropertyMap()).containsEntry("hibernate.format_sql", "true");
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
