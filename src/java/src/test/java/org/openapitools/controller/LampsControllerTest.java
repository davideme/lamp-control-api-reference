package org.openapitools.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.mapper.LampMapper;
import org.openapitools.model.Lamp;
import org.openapitools.model.LampCreate;
import org.openapitools.model.LampUpdate;
import org.openapitools.repository.LampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(LampsController.class)
class LampsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LampRepository lampRepository;

  @MockBean private LampMapper lampMapper;

  @Autowired private ObjectMapper objectMapper;

  private UUID testLampId;
  private Lamp testLamp;
  private LampEntity testLampEntity;

  @BeforeEach
  void setUp() {
    testLampId = UUID.randomUUID();
    testLamp = new Lamp(testLampId, true);
    testLampEntity = new LampEntity(testLampId, true);
  }

  @Test
  void listLamps_ShouldReturnAllLamps() throws Exception {
    // Given
    List<LampEntity> entities = Arrays.asList(testLampEntity);
    when(lampRepository.findAll()).thenReturn(entities);
    when(lampMapper.toModel(testLampEntity)).thenReturn(testLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/lamps").accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testLampId.toString()))
        .andExpect(jsonPath("$[0].status").value(true));
  }

  @Test
  void getLamp_WithValidId_ShouldReturnLamp() throws Exception {
    // Given
    when(lampRepository.findById(testLampId)).thenReturn(Optional.of(testLampEntity));
    when(lampMapper.toModel(testLampEntity)).thenReturn(testLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/lamps/{lampId}", testLampId).accept(MediaType.APPLICATION_JSON))
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
    when(lampRepository.findById(testLampId)).thenReturn(Optional.empty());

    // When & Then
    MvcResult result =
        mockMvc
            .perform(get("/lamps/{lampId}", testLampId).accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
  }

  @Test
  void createLamp_WithValidData_ShouldCreateLamp() throws Exception {
    // Given
    LampCreate lampCreate = new LampCreate();
    lampCreate.setStatus(true);

    when(lampMapper.toEntity(true)).thenReturn(testLampEntity);
    when(lampRepository.save(any(LampEntity.class))).thenReturn(testLampEntity);
    when(lampMapper.toModel(testLampEntity)).thenReturn(testLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                post("/lamps")
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
            post("/lamps")
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
    LampEntity updatedEntity = new LampEntity(testLampId, false);
    Lamp updatedLamp = new Lamp(testLampId, false);

    when(lampRepository.findById(testLampId)).thenReturn(Optional.of(testLampEntity));
    when(lampRepository.save(any(LampEntity.class))).thenReturn(updatedEntity);
    when(lampMapper.toModel(updatedEntity)).thenReturn(updatedLamp);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                put("/lamps/{lampId}", testLampId)
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

    when(lampRepository.findById(testLampId)).thenReturn(Optional.empty());

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                put("/lamps/{lampId}", testLampId)
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
    when(lampRepository.existsById(testLampId)).thenReturn(true);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(delete("/lamps/{lampId}", testLampId))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNoContent());
  }

  @Test
  void deleteLamp_WithNonExistentId_ShouldReturn404() throws Exception {
    // Given
    when(lampRepository.existsById(testLampId)).thenReturn(false);

    // When & Then
    MvcResult result =
        mockMvc
            .perform(delete("/lamps/{lampId}", testLampId))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
  }
}
