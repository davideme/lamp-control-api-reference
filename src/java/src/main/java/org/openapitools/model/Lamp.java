package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Lamp
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.13.0")
public class Lamp {

  private UUID id;

  private Boolean status;

  public Lamp() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Lamp(UUID id, Boolean status) {
    this.id = id;
    this.status = status;
  }

  public Lamp id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Unique identifier for the lamp
   * @return id
   */
  @NotNull @Valid 
  @Schema(name = "id", description = "Unique identifier for the lamp", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Lamp status(Boolean status) {
    this.status = status;
    return this;
  }

  /**
   * Whether the lamp is turned on (true) or off (false)
   * @return status
   */
  @NotNull 
  @Schema(name = "status", description = "Whether the lamp is turned on (true) or off (false)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Lamp lamp = (Lamp) o;
    return Objects.equals(this.id, lamp.id) &&
        Objects.equals(this.status, lamp.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Lamp {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

