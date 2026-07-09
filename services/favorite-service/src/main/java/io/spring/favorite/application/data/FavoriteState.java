package io.spring.favorite.application.data;

import lombok.Value;

@Value
public class FavoriteState {
  private String articleId;
  private boolean favorited;
  private int favoritesCount;
}
