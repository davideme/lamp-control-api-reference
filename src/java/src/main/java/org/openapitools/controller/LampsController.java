package org.openapitools.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.openapitools.api.LampsApi;
import org.openapitools.model.Error;
import org.openapitools.model.Lamp;
import org.openapitools.model.LampCreate;
import org.openapitools.model.LampUpdate;
import org.openapitools.model.ListLamps200Response;
import org.openapitools.service.LampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LampsController implements LampsApi {

  private final LampService lampService;

  @Autowired
  public LampsController(final LampService lampService) {
    this.lampService = lampService;
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> createLamp(final LampCreate lampCreate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final Lamp lamp = new Lamp();
          lamp.setStatus(lampCreate.getStatus());
          final Lamp created = lampService.create(lamp);
          return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<ResponseEntity<Void>> deleteLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            final boolean deleted = lampService.delete(lampUuid);
            if (deleted) {
              return ResponseEntity.noContent().<Void>build();
            } else {
              return ResponseEntity.notFound().<Void>build();
            }
          } catch (IllegalArgumentException e) {
            final Error error = new Error("INVALID_ARGUMENT");
            return (ResponseEntity<Void>)
                (ResponseEntity<?>) ResponseEntity.badRequest().body(error);
          }
        });
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<ResponseEntity<Lamp>> getLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            final Lamp lamp = lampService.findById(lampUuid);
            if (lamp != null) {
              return ResponseEntity.ok().body(lamp);
            } else {
              return ResponseEntity.notFound().build();
            }
          } catch (IllegalArgumentException e) {
            final Error error = new Error("INVALID_ARGUMENT");
            return (ResponseEntity<Lamp>)
                (ResponseEntity<?>) ResponseEntity.badRequest().body(error);
          }
        });
  }

  @Override
  public CompletableFuture<ResponseEntity<ListLamps200Response>> listLamps(
      final Optional<String> cursor, final Optional<Integer> pageSize) {
    return CompletableFuture.supplyAsync(
        () -> {
          final List<Lamp> lamps = lampService.findAllActive();
          final ListLamps200Response response = new ListLamps200Response();
          response.setData(lamps);
          response.setHasMore(false);
          return ResponseEntity.ok().body(response);
        });
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<ResponseEntity<Lamp>> updateLamp(
      final String lampId, final LampUpdate lampUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final UUID lampUuid = UUID.fromString(lampId);
            final Lamp lampData = new Lamp();
            lampData.setStatus(lampUpdate.getStatus());
            final Lamp updated = lampService.update(lampUuid, lampData);
            if (updated != null) {
              return ResponseEntity.ok().body(updated);
            } else {
              return ResponseEntity.notFound().build();
            }
          } catch (IllegalArgumentException e) {
            final Error error = new Error("INVALID_ARGUMENT");
            return (ResponseEntity<Lamp>)
                (ResponseEntity<?>) ResponseEntity.badRequest().body(error);
          }
        });
  }
}
