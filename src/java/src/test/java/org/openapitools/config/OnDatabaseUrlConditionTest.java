package org.openapitools.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.mock.env.MockEnvironment;

/** Unit tests for OnDatabaseUrlCondition and OnNoDatabaseUrlCondition. */
class OnDatabaseUrlConditionTest {

  @Test
  void onDatabaseUrl_WhenUrlIsSet_ShouldMatch() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/lamp");

    ConditionContext context = new MockConditionContext(env);
    OnDatabaseUrlCondition condition = new OnDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isTrue();
  }

  @Test
  void onDatabaseUrl_WhenUrlIsEmpty_ShouldNotMatch() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.datasource.url", "");

    ConditionContext context = new MockConditionContext(env);
    OnDatabaseUrlCondition condition = new OnDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isFalse();
  }

  @Test
  void onDatabaseUrl_WhenUrlIsBlank_ShouldNotMatch() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.datasource.url", "   ");

    ConditionContext context = new MockConditionContext(env);
    OnDatabaseUrlCondition condition = new OnDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isFalse();
  }

  @Test
  void onDatabaseUrl_WhenUrlIsNull_ShouldNotMatch() {
    MockEnvironment env = new MockEnvironment();

    ConditionContext context = new MockConditionContext(env);
    OnDatabaseUrlCondition condition = new OnDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isFalse();
  }

  @Test
  void onNoDatabaseUrl_WhenUrlIsNull_ShouldMatch() {
    MockEnvironment env = new MockEnvironment();

    ConditionContext context = new MockConditionContext(env);
    OnNoDatabaseUrlCondition condition = new OnNoDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isTrue();
  }

  @Test
  void onNoDatabaseUrl_WhenUrlIsEmpty_ShouldMatch() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.datasource.url", "");

    ConditionContext context = new MockConditionContext(env);
    OnNoDatabaseUrlCondition condition = new OnNoDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isTrue();
  }

  @Test
  void onNoDatabaseUrl_WhenUrlIsSet_ShouldNotMatch() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/lamp");

    ConditionContext context = new MockConditionContext(env);
    OnNoDatabaseUrlCondition condition = new OnNoDatabaseUrlCondition();

    ConditionOutcome outcome = condition.getMatchOutcome(context, null);

    assertThat(outcome.isMatch()).isFalse();
  }

  /**
   * Minimal ConditionContext implementation backed by a MockEnvironment. Only getEnvironment() is
   * needed for these conditions.
   */
  private static class MockConditionContext implements ConditionContext {
    private final MockEnvironment environment;

    MockConditionContext(MockEnvironment environment) {
      this.environment = environment;
    }

    @Override
    public org.springframework.beans.factory.config.ConfigurableListableBeanFactory
        getBeanFactory() {
      return null;
    }

    @Override
    public org.springframework.core.env.Environment getEnvironment() {
      return environment;
    }

    @Override
    public org.springframework.core.io.ResourceLoader getResourceLoader() {
      return null;
    }

    @Override
    public org.springframework.beans.factory.support.BeanDefinitionRegistry getRegistry() {
      return null;
    }

    @Override
    public ClassLoader getClassLoader() {
      return getClass().getClassLoader();
    }
  }
}
