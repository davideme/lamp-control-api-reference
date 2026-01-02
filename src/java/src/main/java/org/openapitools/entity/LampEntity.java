package org.openapitools.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity class representing a Lamp in the system. This entity is mapped to the 'lamps' table in the
 * database using JPA annotations.
 */
@Entity
@Table(
    name = "lamps",
    indexes = {
      @Index(name = "idx_lamps_is_on", columnList = "is_on"),
      @Index(name = "idx_lamps_created_at", columnList = "created_at"),
      @Index(name = "idx_lamps_deleted_at", columnList = "deleted_at")
    })
@EntityListeners(AuditingEntityListener.class)
public class LampEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "is_on", nullable = false)
  private Boolean status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  public LampEntity() {}

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

  public OffsetDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(final OffsetDateTime deletedAt) {
    this.deletedAt = deletedAt;
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
