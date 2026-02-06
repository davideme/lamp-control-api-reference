package org.openapitools.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Unit tests for HealthConfiguration.HealthServlet. */
class HealthServletTest {

  @Test
  void doGet_ShouldReturnHealthyJsonResponse() throws Exception {
    // Given
    HealthConfiguration.HealthServlet servlet = new HealthConfiguration.HealthServlet();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // When
    servlet.service(request, response);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentType()).startsWith("application/json");
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(response.getContentAsString()).isEqualTo("{\"status\":\"ok\"}");
  }
}
