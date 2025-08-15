package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * Error
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.13.0")
public class Error {

  private String error;

  private String message;

  private Optional<String> details = Optional.empty();

  public Error() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Error(String error, String message) {
    this.error = error;
    this.message = message;
  }

  public Error error(String error) {
    this.error = error;
    return this;
  }

  /**
   * Error type identifier
   * @return error
   */
  @NotNull 
  @Schema(name = "error", example = "INVALID_ARGUMENT", description = "Error type identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("error")
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Error message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Human-readable error message
   * @return message
   */
  @NotNull 
  @Schema(name = "message", example = "The request contains invalid parameters or malformed data", description = "Human-readable error message", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Error details(String details) {
    this.details = Optional.ofNullable(details);
    return this;
  }

  /**
   * Additional error details or context
   * @return details
   */
  
  @Schema(name = "details", example = "Invalid format for parameter 'status': expected boolean", description = "Additional error details or context", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("details")
  public Optional<String> getDetails() {
    return details;
  }

  public void setDetails(Optional<String> details) {
    this.details = details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.error, error.error) &&
        Objects.equals(this.message, error.message) &&
        Objects.equals(this.details, error.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(error, message, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

