package org.openapitools.exception;

import java.util.UUID;

/**
 * Exception thrown when a lamp with a given ID cannot be found. This is a domain-level exception
 * that maps to HTTP 404 responses.
 */
public class LampNotFoundException extends RuntimeException {

  private final UUID lampId;

  public LampNotFoundException(final UUID lampId) {
    super("Lamp not found: " + lampId);
    this.lampId = lampId;
  }

  public UUID getLampId() {
    return lampId;
  }
}
