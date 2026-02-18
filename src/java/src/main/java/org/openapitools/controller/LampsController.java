package org.openapitools.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.LampsApi;
import org.openapitools.exception.LampNotFoundException;
import org.openapitools.model.Lamp;
import org.openapitools.model.LampCreate;
import org.openapitools.model.LampUpdate;
import org.openapitools.model.ListLamps200Response;
import org.openapitools.service.LampService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1")
@RequiredArgsConstructor
public class LampsController implements LampsApi {

  private final LampService lampService;

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> createLamp(final LampCreate lampCreate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final Lamp lamp = new Lamp();
          lamp.setStatus(lampCreate.getStatus());
          final Lamp created = lampService.create(lamp);
          return ResponseEntity.status(HttpStatus.CREATED).body(created);
        },
        Runnable::run);
  }

  @Override
  public CompletableFuture<ResponseEntity<Void>> deleteLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          final UUID lampUuid = UUID.fromString(lampId);
          lampService.delete(lampUuid);
          return ResponseEntity.noContent().<Void>build();
        },
        Runnable::run);
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> getLamp(final String lampId) {
    return CompletableFuture.supplyAsync(
        () -> {
          final UUID lampUuid = UUID.fromString(lampId);
          final Lamp lamp =
              lampService.findById(lampUuid).orElseThrow(() -> new LampNotFoundException(lampUuid));
          return ResponseEntity.ok().body(lamp);
        },
        Runnable::run);
  }

  @Override
  public CompletableFuture<ResponseEntity<ListLamps200Response>> listLamps(
      final Optional<String> cursor, final Optional<Integer> pageSize) {
    return CompletableFuture.supplyAsync(
        () -> {
          final int offset = parseCursor(cursor);
          final int limit = pageSize.orElse(25);
          final LampService.PagedLampsResult pagedResult =
              lampService.findAllActivePage(offset, limit);
          final ListLamps200Response response = new ListLamps200Response();
          response.setData(pagedResult.data());
          response.setHasMore(pagedResult.hasMore());
          pagedResult.nextCursor().ifPresent(response::nextCursor);
          return ResponseEntity.ok().body(response);
        },
        Runnable::run);
  }

  private int parseCursor(final Optional<String> cursor) {
    if (cursor.isEmpty() || cursor.get().isBlank()) {
      return 0;
    }

    try {
      final int parsed = Integer.parseInt(cursor.get());
      return Math.max(parsed, 0);
    } catch (final NumberFormatException ignored) {
      return 0;
    }
  }

  @Override
  public CompletableFuture<ResponseEntity<Lamp>> updateLamp(
      final String lampId, final LampUpdate lampUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final UUID lampUuid = UUID.fromString(lampId);
          final Lamp lampData = new Lamp();
          lampData.setStatus(lampUpdate.getStatus());
          final Lamp updated = lampService.update(lampUuid, lampData);
          return ResponseEntity.ok().body(updated);
        },
        Runnable::run);
  }
}
