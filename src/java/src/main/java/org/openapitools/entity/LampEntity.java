package org.openapitools.entity;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class representing a Lamp in the system. This is a simple POJO without persistence
 * annotations, suitable for in-memory storage.
 */
public class LampEntity {

  private UUID id;
  private Boolean status;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public LampEntity() {}

  public LampEntity(final Boolean status) {
    this.status = status;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public LampEntity(final UUID lampId, final Boolean status) {
    this.id = lampId;
    this.status = status;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID lampId) {
    this.id = lampId;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(final Boolean status) {
    this.status = status;
    this.updatedAt = OffsetDateTime.now();
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(final OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(final Object obj) {
    boolean result = false;
    if (this == obj) {
      result = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      final LampEntity that = (LampEntity) obj;
      result = Objects.equals(id, that.id) && Objects.equals(status, that.status);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status);
  }

  @Override
  public String toString() {
    return "LampEntity{"
        + "id="
        + id
        + ", status="
        + status
        + ", createdAt="
        + createdAt
        + ", updatedAt="
        + updatedAt
        + '}';
  }
}
