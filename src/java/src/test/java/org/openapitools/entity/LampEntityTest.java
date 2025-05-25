package org.openapitools.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LampEntity class focusing on equals, hashCode, and toString methods to improve
 * code coverage.
 */
class LampEntityTest {

  private UUID testId;
  private LampEntity lampEntity;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
    lampEntity = new LampEntity(testId, true);
  }

  @Test
  void equals_WithSameInstance_ShouldReturnTrue() {
    // When
    final boolean result = lampEntity.equals(lampEntity);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void equals_WithNull_ShouldReturnFalse() {
    // When
    final boolean result = lampEntity.equals(null);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void equals_WithDifferentClass_ShouldReturnFalse() {
    // Given
    final String differentClassObject = "not a LampEntity";

    // When
    final boolean result = lampEntity.equals(differentClassObject);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void equals_WithSameIdAndStatus_ShouldReturnTrue() {
    // Given
    final LampEntity other = new LampEntity(testId, true);

    // When
    final boolean result = lampEntity.equals(other);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void equals_WithDifferentId_ShouldReturnFalse() {
    // Given
    final LampEntity other = new LampEntity(UUID.randomUUID(), true);

    // When
    final boolean result = lampEntity.equals(other);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void equals_WithDifferentStatus_ShouldReturnFalse() {
    // Given
    final LampEntity other = new LampEntity(testId, false);

    // When
    final boolean result = lampEntity.equals(other);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void equals_WithNullIdAndStatus_ShouldWorkCorrectly() {
    // Given
    final LampEntity entity1 = new LampEntity(null, null);
    final LampEntity entity2 = new LampEntity(null, null);

    // When
    final boolean result = entity1.equals(entity2);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void equals_WithOneNullId_ShouldReturnFalse() {
    // Given
    final LampEntity entityWithNullId = new LampEntity(null, true);

    // When
    final boolean result = lampEntity.equals(entityWithNullId);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void equals_WithOneNullStatus_ShouldReturnFalse() {
    // Given
    final LampEntity entityWithNullStatus = new LampEntity(testId, null);

    // When
    final boolean result = lampEntity.equals(entityWithNullStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void hashCode_WithSameValues_ShouldReturnSameHashCode() {
    // Given
    final LampEntity other = new LampEntity(testId, true);

    // When
    final int hash1 = lampEntity.hashCode();
    final int hash2 = other.hashCode();

    // Then
    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void hashCode_WithDifferentValues_ShouldReturnDifferentHashCode() {
    // Given
    final LampEntity other = new LampEntity(UUID.randomUUID(), false);

    // When
    final int hash1 = lampEntity.hashCode();
    final int hash2 = other.hashCode();

    // Then
    assertThat(hash1).isNotEqualTo(hash2);
  }

  @Test
  void hashCode_WithNullValues_ShouldNotThrowException() {
    // Given
    final LampEntity entityWithNulls = new LampEntity(null, null);

    // When/Then - Should not throw an exception
    final int hashCode = entityWithNulls.hashCode();
    assertThat(hashCode).isNotNull();
  }

  @Test
  void toString_ShouldContainIdAndStatus() {
    // When
    final String result = lampEntity.toString();

    // Then
    assertThat(result).isNotNull();
    assertThat(result).contains("LampEntity");
    assertThat(result).contains(testId.toString());
    assertThat(result).contains("true");
    assertThat(result).contains("id=");
    assertThat(result).contains("status=");
  }

  @Test
  void toString_WithNullValues_ShouldHandleGracefully() {
    // Given
    final LampEntity entityWithNulls = new LampEntity(null, null);

    // When
    final String result = entityWithNulls.toString();

    // Then
    assertThat(result).isNotNull();
    assertThat(result).contains("LampEntity");
    assertThat(result).contains("null");
  }

  @Test
  void defaultConstructor_ShouldCreateEntityWithNullValues() {
    // When
    final LampEntity entity = new LampEntity();

    // Then
    assertThat(entity.getId()).isNull();
    assertThat(entity.getStatus()).isNull();
  }

  @Test
  void statusOnlyConstructor_ShouldCreateEntityWithStatus() {
    // When
    final LampEntity entity = new LampEntity(false);

    // Then
    assertThat(entity.getId()).isNull();
    assertThat(entity.getStatus()).isFalse();
  }

  @Test
  void fullConstructor_ShouldCreateEntityWithIdAndStatus() {
    // When
    final LampEntity entity = new LampEntity(testId, false);

    // Then
    assertThat(entity.getId()).isEqualTo(testId);
    assertThat(entity.getStatus()).isFalse();
  }

  @Test
  void setters_ShouldUpdateValues() {
    // Given
    final LampEntity entity = new LampEntity();
    final UUID newId = UUID.randomUUID();

    // When
    entity.setId(newId);
    entity.setStatus(false);

    // Then
    assertThat(entity.getId()).isEqualTo(newId);
    assertThat(entity.getStatus()).isFalse();
  }
}
