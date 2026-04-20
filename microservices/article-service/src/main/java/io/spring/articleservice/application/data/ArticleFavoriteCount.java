package io.spring.articleservice.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFavoriteCount {
  private String id;
  private int count;
}
