package io.spring.infrastructure.clients.mock;

import io.spring.application.clients.ArticleClient;
import io.spring.application.data.ArticleRef;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stand-in for article-service used under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockArticleClient implements ArticleClient {
  @Override
  public Optional<ArticleRef> findBySlug(String slug, String authorization) {
    return Optional.of(new ArticleRef("article-1", slug, "user-1"));
  }
}
