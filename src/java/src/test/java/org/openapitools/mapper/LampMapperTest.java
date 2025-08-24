package org.openapitools.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.entity.LampEntity;
import org.openapitools.model.Lamp;

class LampMapperTest {

  private LampMapper lampMapper;
  private UUID testId;

  @BeforeEach
  void setUp() {
    lampMapper = new LampMapper();
    testId = UUID.randomUUID();
  }

  @Test
  void toModel_WithValidEntity_ShouldReturnModel() {
    // Given
    LampEntity entity = new LampEntity(testId, true);

    // When
    Lamp result = lampMapper.toModel(entity);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testId);
    assertThat(result.getStatus()).isTrue();
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getUpdatedAt()).isNotNull();
  }

  @Test
  void toModel_WithNullEntity_ShouldReturnNull() {
    // When
    Lamp result = lampMapper.toModel(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void toEntity_WithValidModel_ShouldReturnEntity() {
    // Given
    Lamp lamp = new Lamp(testId, false);

    // When
    LampEntity result = lampMapper.toEntity(lamp);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testId);
    assertThat(result.getStatus()).isFalse();
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getUpdatedAt()).isNotNull();
  }

  @Test
  void toEntity_WithNullModel_ShouldReturnNull() {
    // When
    LampEntity result = lampMapper.toEntity((Lamp) null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void toEntity_WithStatusOnly_ShouldReturnEntityWithStatus() {
    // When
    LampEntity result = lampMapper.toEntity(true);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNull(); // Should be null as it's not set in this method
    assertThat(result.getStatus()).isTrue();
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getUpdatedAt()).isNotNull();
  }
}
