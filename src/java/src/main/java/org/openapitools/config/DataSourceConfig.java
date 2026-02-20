package org.openapitools.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
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
@SuppressWarnings("PMD.GodClass")
public class DataSourceConfig {

  @Value("${SPRING_DATASOURCE_URL:}")
  private String springDatasourceUrl;

  @Value("${DATABASE_URL:}")
  private String databaseUrl;

  @Value("${spring.datasource.url}")
  private String fallbackJdbcUrl;

  @Value("${spring.datasource.username:lampuser}")
  private String username;

  @Value("${spring.datasource.password:lamppass}")
  private String password;

  @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
  private String driverClassName;

  @Value("${K_SERVICE:}")
  private String cloudRunService;

  @Value("${K_REVISION:}")
  private String cloudRunRevision;

  /**
   * Creates a HikariConfig bean configured from application.properties.
   *
   * @return configured HikariConfig with core JDBC properties and hikari-specific settings
   */
  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public HikariConfig hikariConfig() {
    HikariConfig config = new HikariConfig();
    String jdbcUrl = resolveJdbcUrl();

    // Set core JDBC properties
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setDriverClassName(driverClassName);
    configureCloudSqlProperties(config, jdbcUrl);

    return config;
  }

  private String resolveJdbcUrl() {
    if (isNotBlank(springDatasourceUrl)) {
      return springDatasourceUrl;
    }

    if (isNotBlank(databaseUrl)) {
      return normalizeDatabaseUrl(databaseUrl);
    }

    return fallbackJdbcUrl;
  }

  private String normalizeDatabaseUrl(String url) {
    if (url.startsWith("jdbc:postgresql://")) {
      return url;
    }
    if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
      return toJdbcPostgresUrl(url);
    }

    return url;
  }

  private String toJdbcPostgresUrl(String url) {
    String normalized =
        url.startsWith("postgres://")
            ? "postgresql://" + url.substring("postgres://".length())
            : url;
    URI uri = URI.create(normalized);

    String host = isNotBlank(uri.getHost()) ? uri.getHost() : "localhost";

    StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
    jdbcUrl.append(host);

    if (uri.getPort() != -1) {
      jdbcUrl.append(':').append(uri.getPort());
    }

    if (isNotBlank(uri.getRawPath())) {
      jdbcUrl.append(uri.getRawPath());
    } else {
      jdbcUrl.append('/');
    }

    String query =
        appendCredentialsIfNeeded(uri.getRawQuery(), extractRawUserInfo(uri, normalized));
    if (isNotBlank(query)) {
      jdbcUrl.append('?').append(query);
    }

    return jdbcUrl.toString();
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  private String appendCredentialsIfNeeded(String rawQuery, String rawUserInfo) {
    if (!isNotBlank(rawUserInfo)) {
      return rawQuery;
    }

    Set<String> existingKeys = queryKeys(rawQuery);
    String[] userInfoParts = rawUserInfo.split(":", 2);
    String user = userInfoParts.length > 0 ? userInfoParts[0] : "";
    String password = userInfoParts.length > 1 ? userInfoParts[1] : "";

    StringBuilder query = new StringBuilder(rawQuery == null ? "" : rawQuery);
    if (!existingKeys.contains("user") && isNotBlank(user)) {
      appendQueryParam(query, "user", user);
    }
    if (!existingKeys.contains("password") && isNotBlank(password)) {
      appendQueryParam(query, "password", password);
    }

    return query.length() == 0 ? null : query.toString();
  }

  private String extractRawUserInfo(URI uri, String rawUrl) {
    if (isNotBlank(uri.getRawUserInfo())) {
      return uri.getRawUserInfo();
    }

    int schemeSeparator = rawUrl.indexOf("://");
    int authorityStart = schemeSeparator >= 0 ? schemeSeparator + 3 : 0;
    int pathStart = rawUrl.indexOf('/', authorityStart);
    int userInfoEnd = rawUrl.indexOf('@', authorityStart);

    if (userInfoEnd > authorityStart && (pathStart == -1 || userInfoEnd < pathStart)) {
      return rawUrl.substring(authorityStart, userInfoEnd);
    }

    return null;
  }

  private Set<String> queryKeys(String rawQuery) {
    Set<String> keys = new LinkedHashSet<>();
    if (!isNotBlank(rawQuery)) {
      return keys;
    }

    for (String part : rawQuery.split("&")) {
      if (!isNotBlank(part)) {
        continue;
      }

      int separator = part.indexOf('=');
      String key = separator >= 0 ? part.substring(0, separator) : part;
      if (isNotBlank(key)) {
        keys.add(key);
      }
    }

    return keys;
  }

  private void appendQueryParam(StringBuilder query, String key, String value) {
    if (query.length() > 0) {
      query.append('&');
    }
    query.append(key).append('=').append(value);
  }

  private boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }

  private void configureCloudSqlProperties(HikariConfig config, String jdbcUrl) {
    String instanceUnixSocket = extractUnixSocketPath(jdbcUrl);
    if (!isNotBlank(instanceUnixSocket)) {
      return;
    }

    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
    config.addDataSourceProperty("unixSocketPath", instanceUnixSocket);
    String cloudSqlInstance = extractCloudSqlInstance(instanceUnixSocket);
    if (isNotBlank(cloudSqlInstance)) {
      config.addDataSourceProperty("cloudSqlInstance", cloudSqlInstance);
    }

    if (isCloudRun()) {
      config.addDataSourceProperty("cloudSqlRefreshStrategy", "lazy");
    }
  }

  private String extractCloudSqlInstance(String unixSocketPath) {
    final String prefix = "/cloudsql/";
    if (!isNotBlank(unixSocketPath) || !unixSocketPath.startsWith(prefix)) {
      return null;
    }
    String instanceConnectionName = unixSocketPath.substring(prefix.length());
    return isNotBlank(instanceConnectionName) ? instanceConnectionName : null;
  }

  private String extractUnixSocketPath(String jdbcUrl) {
    String rawQuery = extractRawQuery(jdbcUrl);
    if (!isNotBlank(rawQuery)) {
      return null;
    }

    String host = extractQueryParam(rawQuery, "host");
    if (isNotBlank(host) && decodeQueryValue(host).startsWith("/cloudsql/")) {
      return decodeQueryValue(host);
    }

    String unixSocketPath = extractQueryParam(rawQuery, "unixSocketPath");
    if (isNotBlank(unixSocketPath)) {
      return decodeQueryValue(unixSocketPath);
    }

    return null;
  }

  private String extractRawQuery(String jdbcUrl) {
    int queryStart = jdbcUrl.indexOf('?');
    if (queryStart < 0 || queryStart + 1 >= jdbcUrl.length()) {
      return null;
    }
    return jdbcUrl.substring(queryStart + 1);
  }

  private String extractQueryParam(String rawQuery, String key) {
    for (String part : rawQuery.split("&")) {
      if (!isNotBlank(part)) {
        continue;
      }

      int separator = part.indexOf('=');
      if (separator < 0) {
        continue;
      }

      String partKey = part.substring(0, separator);
      if (key.equals(partKey)) {
        return part.substring(separator + 1);
      }
    }
    return null;
  }

  private String decodeQueryValue(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private boolean isCloudRun() {
    return isNotBlank(cloudRunService) || isNotBlank(cloudRunRevision);
  }

  /**
   * Creates a HikariCP DataSource bean from the configured HikariConfig.
   *
   * @param hikariConfig the HikariConfig bean with all properties already applied
   * @return configured DataSource using HikariCP connection pooling
   */
  @Bean
  public DataSource dataSource(HikariConfig hikariConfig) {
    return new HikariDataSource(hikariConfig);
  }
}
