package io.spring.favorite.application.data;

import lombok.Value;

@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
