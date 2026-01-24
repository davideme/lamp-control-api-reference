package org.openapitools.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * JPA and Hibernate configuration for PostgreSQL database access.
 *
 * <p>This configuration is only activated when a DataSource bean is available (i.e., when
 * spring.datasource.url is set). When no database is configured, JPA is not initialized and the
 * application uses an in-memory repository.
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@EnableJpaRepositories(basePackages = "org.openapitools.repository")
public class JpaConfig {

  @Value("${spring.jpa.hibernate.ddl-auto:validate}")
  private String ddlAuto;

  @Value("${spring.jpa.show-sql:false}")
  private boolean showSql;

  @Value("${spring.jpa.properties.hibernate.format_sql:true}")
  private boolean formatSql;

  @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
  private String dialect;

  @Value("${spring.jpa.properties.hibernate.jdbc.time_zone:UTC}")
  private String timeZone;

  /**
   * Creates the EntityManagerFactory for JPA.
   *
   * @param dataSource the DataSource to use
   * @return configured LocalContainerEntityManagerFactoryBean
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("org.openapitools.entity");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setShowSql(showSql);
    em.setJpaVendorAdapter(vendorAdapter);

    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
    properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
    properties.setProperty("hibernate.dialect", dialect);
    properties.setProperty("hibernate.jdbc.time_zone", timeZone);

    em.setJpaProperties(properties);

    return em;
  }

  /**
   * Creates the JPA transaction manager.
   *
   * @param dataSource the DataSource to use
   * @return configured PlatformTransactionManager
   */
  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory(dataSource).getObject());
    return transactionManager;
  }
}
