package io.spring.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Write payload for favoriting/unfavoriting keyed by referenced IDs. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {
  private String userId;
  private String articleId;
}
