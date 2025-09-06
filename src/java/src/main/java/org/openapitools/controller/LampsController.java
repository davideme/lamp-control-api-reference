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
import org.openapitools.model.ListLamps200Response;
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
  public LampsController(final LampRepository lampRepository, final LampMapper lampMapper) {
    this.lampRepository = lampRepository;
    this.lampMapper = lampMapper;
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> createLamp(final LampCreate lampCreate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final LampEntity entity = lampMapper.toEntity(lampCreate.getStatus());
          final LampEntity savedEntity = lampRepository.save(entity);
          final Lamp lamp = lampMapper.toModel(savedEntity);
          return ResponseEntity.status(HttpStatus.CREATED).body(lamp);
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<Void>> deleteLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            if (lampRepository.existsById(lampUuid)) {
              lampRepository.deleteById(lampUuid);
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
  public CompletableFuture<ResponseEntity<Lamp>> getLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            final Optional<LampEntity> entity = lampRepository.findById(lampUuid);
            return entity
                .map(
                    lampEntity -> {
                      final Lamp lamp = lampMapper.toModel(lampEntity);
                      return ResponseEntity.ok().body(lamp);
                    })
                .orElse(ResponseEntity.notFound().build());
          } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
          }
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<ListLamps200Response>> listLamps(
      final Optional<String> cursor, final Optional<Integer> pageSize) {
    return CompletableFuture.supplyAsync(
        () -> {
          final List<LampEntity> entities = lampRepository.findAll();
          final List<Lamp> lamps =
              entities.stream().map(lampMapper::toModel).collect(Collectors.toList());
          final ListLamps200Response response = new ListLamps200Response();
          response.setData(lamps);
          response.setHasMore(false);
          return ResponseEntity.ok().body(response);
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> updateLamp(
      final String lampId, final LampUpdate lampUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            final Optional<LampEntity> existingEntity = lampRepository.findById(lampUuid);
            return existingEntity
                .map(
                    entity -> {
                      entity.setStatus(lampUpdate.getStatus());
                      final LampEntity savedEntity = lampRepository.save(entity);
                      final Lamp lamp = lampMapper.toModel(savedEntity);
                      return ResponseEntity.ok().body(lamp);
                    })
                .orElse(ResponseEntity.notFound().build());
          } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
          }
        });
  }
}
