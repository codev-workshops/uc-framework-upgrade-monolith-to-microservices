package io.spring.application.clients;

import io.spring.application.data.ArticleRef;
import java.util.Optional;

/** Resolves articles owned by article-service. */
public interface ArticleClient {
  /**
   * Resolve an article by slug via article-service {@code GET /internal/articles/{slug}}. Returns
   * empty when the article does not exist (404).
   */
  Optional<ArticleRef> findBySlug(String slug, String authorization);
}
