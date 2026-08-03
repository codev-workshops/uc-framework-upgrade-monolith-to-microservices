package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single article-to-tag association, used to build batch tag lookups. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagData {
  private String articleId;
  private String tagName;
}
