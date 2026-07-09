package io.spring.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Internal slug->id resolution incl. authorId for authorization (comment-service + favorite). */
@Getter
@AllArgsConstructor
public class ArticleRef {
  private final String id;
  private final String slug;
  private final String authorId;
}
