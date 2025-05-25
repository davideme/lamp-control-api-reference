package org.openapitools.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity class representing a Lamp in the system. This is a simple POJO without
 * persistence
 * annotations, suitable for in-memory storage.
 */
public class LampEntity {

  private UUID id;
  private Boolean status;

  public LampEntity() {
  }

  public LampEntity(final Boolean status) {
    this.status = status;
  }

  public LampEntity(final UUID lampId, final Boolean status) {
    this.id = lampId;
    this.status = status;
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
    return "LampEntity{" + "id=" + id + ", status=" + status + '}';
  }
}
