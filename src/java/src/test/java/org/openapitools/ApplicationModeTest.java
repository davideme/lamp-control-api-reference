package org.openapitools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ApplicationMode. Tests the mode parsing and configuration logic without starting a
 * Spring context.
 */
class ApplicationModeTest {

  @AfterEach
  void tearDown() {
    System.clearProperty("spring.flyway.enabled");
  }

  @Test
  void parseMode_WithServeMode_ShouldReturnServe() {
    String[] args = {"--mode=serve"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.SERVE);
  }

  @Test
  void parseMode_WithServeOnlyMode_ShouldReturnServeOnly() {
    String[] args = {"--mode=serve-only"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.SERVE_ONLY);
  }

  @Test
  void parseMode_WithMigrateMode_ShouldReturnMigrate() {
    String[] args = {"--mode=migrate"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.MIGRATE);
  }

  @Test
  void parseMode_WithNoModeArg_ShouldReturnServeOnlyAsDefault() {
    String[] args = {};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.SERVE_ONLY);
  }

  @Test
  void parseMode_WithOtherArgs_ShouldReturnServeOnlyAsDefault() {
    String[] args = {"--port=8080", "--verbose"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.SERVE_ONLY);
  }

  @Test
  void parseMode_WithUpperCaseMode_ShouldReturnCorrectMode() {
    String[] args = {"--mode=SERVE"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.SERVE);
  }

  @Test
  void parseMode_WithMixedCaseMode_ShouldReturnCorrectMode() {
    String[] args = {"--mode=Migrate"};
    assertThat(ApplicationMode.parseMode(args)).isEqualTo(ApplicationMode.Mode.MIGRATE);
  }

  @Test
  void configureMode_WithServe_ShouldEnableFlyway() {
    ApplicationMode.configureMode(ApplicationMode.Mode.SERVE);
    assertThat(System.getProperty("spring.flyway.enabled")).isEqualTo("true");
  }

  @Test
  void configureMode_WithServeOnly_ShouldDisableFlyway() {
    ApplicationMode.configureMode(ApplicationMode.Mode.SERVE_ONLY);
    assertThat(System.getProperty("spring.flyway.enabled")).isEqualTo("false");
  }

  @Test
  void configureMode_WithMigrate_ShouldNotSetFlywayProperty() {
    System.clearProperty("spring.flyway.enabled");
    ApplicationMode.configureMode(ApplicationMode.Mode.MIGRATE);
    assertThat(System.getProperty("spring.flyway.enabled")).isNull();
  }

  @Test
  void modeEnum_ShouldHaveThreeValues() {
    assertThat(ApplicationMode.Mode.values()).hasSize(3);
    assertThat(ApplicationMode.Mode.values())
        .containsExactly(
            ApplicationMode.Mode.SERVE_ONLY,
            ApplicationMode.Mode.SERVE,
            ApplicationMode.Mode.MIGRATE);
  }
}
