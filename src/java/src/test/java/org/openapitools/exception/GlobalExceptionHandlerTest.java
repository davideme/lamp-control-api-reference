package org.openapitools.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.openapitools.controller.LampsController;
import org.openapitools.mapper.LampMapper;
import org.openapitools.repository.LampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for GlobalExceptionHandler. Tests that validation errors are properly converted to
 * HTTP 400 responses with the correct error format.
 */
@WebMvcTest(LampsController.class)
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LampRepository lampRepository;

  @MockBean private LampMapper lampMapper;

  @Test
  void testInvalidPageSizeNegative() throws Exception {
    mockMvc
        .perform(get("/v1/lamps").param("pageSize", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
  }

  @Test
  void testInvalidPageSizeZero() throws Exception {
    mockMvc
        .perform(get("/v1/lamps").param("pageSize", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
  }

  @Test
  void testInvalidPageSizeTooLarge() throws Exception {
    mockMvc
        .perform(get("/v1/lamps").param("pageSize", "101"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
  }

  @Test
  void testInvalidPageSizeNotNumeric() throws Exception {
    mockMvc
        .perform(get("/v1/lamps").param("pageSize", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
  }
}
