package org.openapitools.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for LampNotFoundException. */
class LampNotFoundExceptionTest {

  @Test
  void shouldContainLampIdInMessage() {
    final UUID lampId = UUID.randomUUID();

    final LampNotFoundException ex = new LampNotFoundException(lampId);

    assertThat(ex.getMessage()).contains(lampId.toString());
    assertThat(ex.getLampId()).isEqualTo(lampId);
  }
}
