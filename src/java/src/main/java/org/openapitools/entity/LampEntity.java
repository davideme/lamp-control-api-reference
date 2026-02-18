package org.openapitools.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

/**
 * Entity class representing a Lamp in the system. This entity is mapped to the 'lamps' table in the
 * database using JPA annotations.
 *
 * <p>Supports soft deletes via the deletedAt field. Soft-deleted entities are automatically
 * filtered from queries by the @Where clause.
 */
@Entity
@Table(name = "lamps")
@Where(clause = "deleted_at IS NULL")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "status"})
public class LampEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
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

  public LampEntity(final Boolean status) {
    this.status = status;
  }

  public LampEntity(final UUID lampId, final Boolean status) {
    this.id = lampId;
    this.status = status;
  }
}
