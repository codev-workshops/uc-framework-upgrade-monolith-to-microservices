package io.spring.shared.client;

import io.spring.shared.dto.ArticleData;
import java.util.Optional;

/**
 * Client interface for calling the Article Service's internal API. Implementations may use Feign or
 * RestTemplate.
 */
public interface ArticleServiceClient {

  /** Get article by slug. */
  Optional<ArticleData> getArticleBySlug(String slug);

  /** Get article by ID. Returns minimal article info (id, slug, title, userId). */
  Optional<ArticleData> getArticleById(String id);
}
