package org.openapitools.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Conditionally enables database auto-configuration only when DATABASE_URL is provided. When no
 * database URL is configured, the application uses the in-memory repository instead.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@Import({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class DatabaseAutoConfiguration {}
