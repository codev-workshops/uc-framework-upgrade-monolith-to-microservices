package io.spring.client.mock;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.client.FavoriteClient;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stub used when running standalone under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockFavoriteClient implements FavoriteClient {

  @Override
  public List<ArticleFavoriteCount> countsForArticles(List<String> articleIds) {
    return articleIds.stream()
        .map(id -> new ArticleFavoriteCount(id, 0))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> favoritedArticles(String userId, List<String> articleIds) {
    return Collections.emptyList();
  }

  @Override
  public List<String> articlesFavoritedByUser(String userId) {
    return Collections.emptyList();
  }
}
