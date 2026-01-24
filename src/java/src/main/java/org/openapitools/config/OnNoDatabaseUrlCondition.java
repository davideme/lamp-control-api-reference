package org.openapitools.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Custom condition that checks if spring.datasource.url is NOT set or is empty.
 *
 * <p>This is the inverse of OnDatabaseUrlCondition. It's used to enable in-memory repository when
 * no database is configured.
 */
public class OnNoDatabaseUrlCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    String url = context.getEnvironment().getProperty("spring.datasource.url");

    if (url == null || url.trim().isEmpty()) {
      return ConditionOutcome.match(
          "spring.datasource.url is not set or is empty (using in-memory repository)");
    }

    return ConditionOutcome.noMatch("spring.datasource.url is set to: " + url);
  }
}
