package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal slug-&gt;id resolution returned by article-service {@code GET
 * /internal/articles/{slug}}, including {@code authorId} for authorization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleRef {
  private String id;
  private String slug;
  private String authorId;
}
