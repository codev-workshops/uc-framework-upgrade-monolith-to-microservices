package io.spring.shared.client;

import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;

/**
 * Client interface for calling the Favorite Service's internal API. Implementations may use Feign
 * or RestTemplate.
 */
public interface FavoriteServiceClient {

  /** Get favorite count for a single article. */
  int getFavoriteCount(String articleId);

  /** Check if a user has favorited a specific article. */
  boolean isUserFavorite(String userId, String articleId);

  /** Get favorite counts for multiple articles. */
  List<ArticleFavoriteCount> getFavoriteCounts(List<String> articleIds);

  /** Get set of article IDs that the user has favorited, from the given list. */
  Set<String> getUserFavorites(List<String> articleIds, String userId);
}
