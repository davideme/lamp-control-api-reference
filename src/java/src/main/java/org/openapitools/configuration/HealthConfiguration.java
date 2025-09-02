package org.openapitools.configuration;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Configuration to provide health endpoint at root level */
@Configuration
public class HealthConfiguration {

  /** Register a servlet for the health endpoint at root level, bypassing the context path */
  @Bean
  public ServletRegistrationBean<HttpServlet> healthServletRegistration() {
    ServletRegistrationBean<HttpServlet> registration = new ServletRegistrationBean<>();
    registration.setServlet(new HealthServlet());
    registration.addUrlMappings("/health");
    registration.setName("healthServlet");
    return registration;
  }

  /** Simple servlet providing health status at root level */
  public static class HealthServlet extends HttpServlet {

    private static final String HEALTH_RESPONSE = "{\"status\":\"ok\"}";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write(HEALTH_RESPONSE);
    }
  }

  /** Health controller providing service status - kept for context path aware access */
  @RestController
  public static class HealthController {

    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Map<String, String>> health() {
      return ResponseEntity.ok(Map.of("status", "ok"));
    }
  }
}
