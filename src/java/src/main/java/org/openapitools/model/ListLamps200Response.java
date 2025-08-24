package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.Lamp;
import org.springframework.lang.Nullable;
import java.util.NoSuchElementException;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ListLamps200Response
 */

@JsonTypeName("listLamps_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.13.0")
public class ListLamps200Response {

  @Valid
  private List<@Valid Lamp> data = new ArrayList<>();

  private JsonNullable<String> nextCursor = JsonNullable.<String>undefined();

  private Boolean hasMore;

  public ListLamps200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ListLamps200Response(List<@Valid Lamp> data, Boolean hasMore) {
    this.data = data;
    this.hasMore = hasMore;
  }

  public ListLamps200Response data(List<@Valid Lamp> data) {
    this.data = data;
    return this;
  }

  public ListLamps200Response addDataItem(Lamp dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public List<@Valid Lamp> getData() {
    return data;
  }

  public void setData(List<@Valid Lamp> data) {
    this.data = data;
  }

  public ListLamps200Response nextCursor(String nextCursor) {
    this.nextCursor = JsonNullable.of(nextCursor);
    return this;
  }

  /**
   * Get nextCursor
   * @return nextCursor
   */
  
  @Schema(name = "nextCursor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nextCursor")
  public JsonNullable<String> getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(JsonNullable<String> nextCursor) {
    this.nextCursor = nextCursor;
  }

  public ListLamps200Response hasMore(Boolean hasMore) {
    this.hasMore = hasMore;
    return this;
  }

  /**
   * Get hasMore
   * @return hasMore
   */
  @NotNull 
  @Schema(name = "hasMore", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("hasMore")
  public Boolean getHasMore() {
    return hasMore;
  }

  public void setHasMore(Boolean hasMore) {
    this.hasMore = hasMore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListLamps200Response listLamps200Response = (ListLamps200Response) o;
    return Objects.equals(this.data, listLamps200Response.data) &&
        equalsNullable(this.nextCursor, listLamps200Response.nextCursor) &&
        Objects.equals(this.hasMore, listLamps200Response.hasMore);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, hashCodeNullable(nextCursor), hasMore);
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListLamps200Response {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    nextCursor: ").append(toIndentedString(nextCursor)).append("\n");
    sb.append("    hasMore: ").append(toIndentedString(hasMore)).append("\n");
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

