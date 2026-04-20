package io.spring.articleservice.core.favorite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFavorite {
  private String articleId;
  private String userId;
}
