package org.openapitools.configuration;

import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Configuration to provide health endpoint at root level */
@Configuration
public class HealthConfiguration {

  /** Health controller providing service status */
  @RestController
  public static class HealthController {

    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Map<String, String>> health() {
      return ResponseEntity.ok(Map.of("status", "ok"));
    }
  }
}
