package org.openapitools.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

/** Unit tests for HealthConfiguration bean registration. */
class HealthConfigurationTest {

  @Test
  void healthServletRegistration_ShouldConfigureServletCorrectly() {
    HealthConfiguration config = new HealthConfiguration();

    ServletRegistrationBean<HttpServlet> registration = config.healthServletRegistration();

    assertThat(registration).isNotNull();
    assertThat(registration.getServletName()).isEqualTo("healthServlet");
    assertThat(registration.getUrlMappings()).contains("/health");
  }
}
