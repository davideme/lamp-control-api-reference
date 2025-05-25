package org.openapitools.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.openapitools.api.LampsApi;
import org.openapitools.entity.LampEntity;
import org.openapitools.mapper.LampMapper;
import org.openapitools.model.Lamp;
import org.openapitools.model.LampCreate;
import org.openapitools.model.LampUpdate;
import org.openapitools.repository.LampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LampsController implements LampsApi {

  private final LampRepository lampRepository;
  private final LampMapper lampMapper;

  @Autowired
  public LampsController(LampRepository lampRepository, LampMapper lampMapper) {
    this.lampRepository = lampRepository;
    this.lampMapper = lampMapper;
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> createLamp(LampCreate lampCreate) {
    return CompletableFuture.supplyAsync(
        () -> {
          LampEntity entity = lampMapper.toEntity(lampCreate.getStatus());
          LampEntity savedEntity = lampRepository.save(entity);
          Lamp lamp = lampMapper.toModel(savedEntity);
          return ResponseEntity.status(HttpStatus.CREATED).body(lamp);
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<Void>> deleteLamp(String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            UUID id = UUID.fromString(lampId);
            if (lampRepository.existsById(id)) {
              lampRepository.deleteById(id);
              return ResponseEntity.noContent().<Void>build();
            } else {
              return ResponseEntity.notFound().<Void>build();
            }
          } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().<Void>build();
          }
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> getLamp(String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            UUID id = UUID.fromString(lampId);
            Optional<LampEntity> entity = lampRepository.findById(id);
            return entity
                .map(
                    lampEntity -> {
                      Lamp lamp = lampMapper.toModel(lampEntity);
                      return ResponseEntity.ok().body(lamp);
                    })
                .orElse(ResponseEntity.notFound().build());
          } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
          }
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<List<Lamp>>> listLamps() {
    return CompletableFuture.supplyAsync(
        () -> {
          List<LampEntity> entities = lampRepository.findAll();
          List<Lamp> lamps =
              entities.stream().map(lampMapper::toModel).collect(Collectors.toList());
          return ResponseEntity.ok().body(lamps);
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> updateLamp(String lampId, LampUpdate lampUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            UUID id = UUID.fromString(lampId);
            Optional<LampEntity> existingEntity = lampRepository.findById(id);
            return existingEntity
                .map(
                    entity -> {
                      entity.setStatus(lampUpdate.getStatus());
                      LampEntity savedEntity = lampRepository.save(entity);
                      Lamp lamp = lampMapper.toModel(savedEntity);
                      return ResponseEntity.ok().body(lamp);
                    })
                .orElse(ResponseEntity.notFound().build());
          } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
          }
        });
  }
}
