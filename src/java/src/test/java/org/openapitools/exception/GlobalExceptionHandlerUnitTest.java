package org.openapitools.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Direct unit tests for GlobalExceptionHandler methods not easily triggered via MockMvc. */
class GlobalExceptionHandlerUnitTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleNullPointerException_ShouldReturnBadRequest() {
    NullPointerException ex = new NullPointerException("test null");

    ResponseEntity<Error> response = handler.handleNullPointerException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo("INVALID_ARGUMENT");
  }

  @Test
  void handleLampNotFoundException_ShouldReturnNotFound() {
    LampNotFoundException ex = new LampNotFoundException(UUID.randomUUID());

    ResponseEntity<Void> response = handler.handleLampNotFoundException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void handleIllegalArgumentException_ShouldReturnBadRequest() {
    IllegalArgumentException ex = new IllegalArgumentException("bad argument");

    ResponseEntity<Error> response = handler.handleIllegalArgumentException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo("INVALID_ARGUMENT");
  }
}
