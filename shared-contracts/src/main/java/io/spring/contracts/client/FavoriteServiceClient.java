package io.spring.contracts.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Contract for favorite counts and per-user favorite state from favorite-service. */
public interface FavoriteServiceClient {
  int articleFavoriteCount(String articleId);

  /** articleId -> favorite count, for a batch of articles. */
  Map<String, Integer> favoriteCounts(List<String> articleIds);

  boolean isFavorited(String userId, String articleId);

  /** Subset of {@code articleIds} favorited by {@code userId} (batch read composition). */
  Set<String> userFavorites(List<String> articleIds, String userId);
}
