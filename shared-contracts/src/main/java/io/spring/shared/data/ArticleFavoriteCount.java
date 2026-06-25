package io.spring.shared.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ArticleFavoriteCount {
  private final String id;
  private final int count;

  @JsonCreator
  public ArticleFavoriteCount(
      @JsonProperty("id") String id,
      @JsonProperty("count") int count) {
    this.id = id;
    this.count = count;
  }
}
