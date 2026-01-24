package org.openapitools.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Custom condition that checks if spring.datasource.url is set to a non-empty value.
 *
 * <p>This condition is used to enable database configuration only when a database URL is actually
 * configured. Unlike @ConditionalOnProperty which activates when the property exists (even if
 * empty), this condition requires a non-empty value.
 */
public class OnDatabaseUrlCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    String url = context.getEnvironment().getProperty("spring.datasource.url");

    if (url != null && !url.trim().isEmpty()) {
      return ConditionOutcome.match("spring.datasource.url is set to: " + url);
    }

    return ConditionOutcome.noMatch(
        "spring.datasource.url is not set or is empty (using in-memory repository)");
  }
}
