package io.spring.application;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.contracts.client.FavoriteServiceClient;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Read side backing the {@link FavoriteServiceClient} contract, keyed purely by referenced IDs. */
@Service
@AllArgsConstructor
public class FavoriteQueryService implements FavoriteServiceClient {

  private final ArticleFavoritesReadService readService;

  @Override
  public int articleFavoriteCount(String articleId) {
    return readService.articleFavoriteCount(articleId);
  }

  @Override
  public Map<String, Integer> favoriteCounts(List<String> articleIds) {
    Map<String, Integer> result = new HashMap<>();
    if (articleIds == null || articleIds.isEmpty()) {
      return result;
    }
    for (String id : articleIds) {
      result.put(id, 0);
    }
    for (ArticleFavoriteCount count : readService.articlesFavoriteCount(articleIds)) {
      result.put(count.getId(), count.getCount());
    }
    return result;
  }

  @Override
  public boolean isFavorited(String userId, String articleId) {
    return readService.isUserFavorite(userId, articleId);
  }

  @Override
  public Set<String> userFavorites(List<String> articleIds, String userId) {
    if (articleIds == null || articleIds.isEmpty()) {
      return new HashSet<>();
    }
    return readService.userFavorites(articleIds, userId);
  }
}
