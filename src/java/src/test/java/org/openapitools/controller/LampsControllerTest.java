package org.openapitools.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Lamp;
import org.openapitools.model.LampCreate;
import org.openapitools.model.LampUpdate;
import org.openapitools.service.LampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(LampsController.class)
class LampsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LampService lampService;

  @Autowired private ObjectMapper objectMapper;

  private UUID testLampId;
  private Lamp testLamp;

  @BeforeEach
  void setUp() {
    testLampId = UUID.randomUUID();
    testLamp = new Lamp(testLampId, true);
  }

  @Test
  void listLamps_ShouldReturnAllLamps() throws Exception {
    // Given
    List<Lamp> lamps = Arrays.asList(testLamp);
    when(lampService.findAllActive()).thenReturn(lamps);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/v1/lamps").accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(testLampId.toString()))
        .andExpect(jsonPath("$.data[0].status").value(true));
  }

  @Test
  void getLamp_WithValidId_ShouldReturnLamp() throws Exception {
    // Given
    when(lampService.findById(testLampId)).thenReturn(testLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/v1/lamps/{lampId}", testLampId).accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testLampId.toString()))
        .andExpect(jsonPath("$.status").value(true));
  }

  @Test
  void getLamp_WithNonExistentId_ShouldReturn404() throws Exception {
    // Given
    when(lampService.findById(testLampId)).thenReturn(null);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/v1/lamps/{lampId}", testLampId).accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
  }

  @Test
  void createLamp_WithValidData_ShouldCreateLamp() throws Exception {
    // Given
    LampCreate lampCreate = new LampCreate();
    lampCreate.setStatus(true);

    when(lampService.create(any(Lamp.class))).thenReturn(testLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                post("/v1/lamps")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lampCreate))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testLampId.toString()))
        .andExpect(jsonPath("$.status").value(true));
  }

  @Test
  void createLamp_WithInvalidData_ShouldReturn400() throws Exception {
    // Given - empty body

    // When & Then
    mockMvc
        .perform(
            post("/v1/lamps")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateLamp_WithValidData_ShouldUpdateLamp() throws Exception {
    // Given
    LampUpdate lampUpdate = new LampUpdate();
    lampUpdate.setStatus(false);
    Lamp updatedLamp = new Lamp(testLampId, false);

    when(lampService.update(any(UUID.class), any(Lamp.class))).thenReturn(updatedLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                put("/v1/lamps/{lampId}", testLampId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lampUpdate))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testLampId.toString()))
        .andExpect(jsonPath("$.status").value(false));
  }

  @Test
  void updateLamp_WithNonExistentId_ShouldReturn404() throws Exception {
    // Given
    LampUpdate lampUpdate = new LampUpdate();
    lampUpdate.setStatus(false);

    when(lampService.update(any(UUID.class), any(Lamp.class))).thenReturn(null);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                put("/v1/lamps/{lampId}", testLampId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lampUpdate))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
  }

  @Test
  void deleteLamp_WithValidId_ShouldDelete() throws Exception {
    // Given
    when(lampService.delete(testLampId)).thenReturn(true);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(delete("/v1/lamps/{lampId}", testLampId))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNoContent());
  }

  @Test
  void deleteLamp_WithNonExistentId_ShouldReturn404() throws Exception {
    // Given
    when(lampService.delete(testLampId)).thenReturn(false);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(delete("/v1/lamps/{lampId}", testLampId))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
  }

  @Test
  void getLamp_WithInvalidUuid_ShouldReturn400() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/v1/lamps/{lampId}", "not-a-uuid").accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isBadRequest());
  }

  @Test
  void updateLamp_WithInvalidUuid_ShouldReturn400() throws Exception {
    LampUpdate lampUpdate = new LampUpdate();
    lampUpdate.setStatus(false);

    MvcResult result =
        mockMvc
            .perform(
                put("/v1/lamps/{lampId}", "not-a-uuid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lampUpdate))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isBadRequest());
  }

  @Test
  void deleteLamp_WithInvalidUuid_ShouldReturn400() throws Exception {
    MvcResult result =
        mockMvc
            .perform(delete("/v1/lamps/{lampId}", "not-a-uuid"))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isBadRequest());
  }
}
