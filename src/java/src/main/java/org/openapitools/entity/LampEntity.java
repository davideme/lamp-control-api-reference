package org.openapitools.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity class representing a Lamp in the system. This is a simple POJO without persistence
 * annotations, suitable for in-memory storage.
 */
public class LampEntity {

  private UUID id;
  private Boolean status;

  public LampEntity() {}

  public LampEntity(Boolean status) {
    this.status = status;
  }

  public LampEntity(UUID id, Boolean status) {
    this.id = id;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LampEntity that = (LampEntity) o;
    return Objects.equals(id, that.id) && Objects.equals(status, that.status);
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
