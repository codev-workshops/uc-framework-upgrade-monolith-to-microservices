package io.spring.client;

import io.spring.application.data.ArticleFavoriteCount;
import java.util.List;

/** Read-only view of favorite-service used to compose favoritesCount / favorited. */
public interface FavoriteClient {

  /** {@code POST /internal/favorites/counts} -> counts for the given article ids. */
  List<ArticleFavoriteCount> countsForArticles(List<String> articleIds);

  /**
   * {@code POST /internal/favorites/status?userId=} -> the subset of the given article ids that the
   * user has favorited.
   */
  List<String> favoritedArticles(String userId, List<String> articleIds);

  /** {@code GET /internal/favorites/by-user?userId=} -> all article ids favorited by the user. */
  List<String> articlesFavoritedByUser(String userId);
}
