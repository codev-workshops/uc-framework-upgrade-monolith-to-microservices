package io.spring.favorite.application;

import io.spring.favorite.application.data.ArticleFavoriteCount;
import io.spring.favorite.application.data.FavoriteState;
import io.spring.favorite.core.ArticleFavorite;
import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {
  private final ArticleFavoriteRepository articleFavoriteRepository;
  private final ArticleFavoritesReadService readService;

  public FavoriteService(
      ArticleFavoriteRepository articleFavoriteRepository,
      ArticleFavoritesReadService readService) {
    this.articleFavoriteRepository = articleFavoriteRepository;
    this.readService = readService;
  }

  public FavoriteState favorite(String articleId, String userId) {
    articleFavoriteRepository.save(new ArticleFavorite(articleId, userId));
    return state(articleId, userId);
  }

  public FavoriteState unfavorite(String articleId, String userId) {
    articleFavoriteRepository
        .find(articleId, userId)
        .ifPresent(articleFavoriteRepository::remove);
    return state(articleId, userId);
  }

  private FavoriteState state(String articleId, String userId) {
    boolean favorited = readService.isUserFavorite(userId, articleId);
    int count = readService.articleFavoriteCount(articleId);
    return new FavoriteState(articleId, favorited, count);
  }

  public int count(String articleId) {
    return readService.articleFavoriteCount(articleId);
  }

  /** Returns a count for every requested id, defaulting to 0 when no favorites exist. */
  public List<ArticleFavoriteCount> counts(List<String> articleIds) {
    List<ArticleFavoriteCount> result = new ArrayList<>();
    if (articleIds == null || articleIds.isEmpty()) {
      return result;
    }
    Map<String, Integer> found = new HashMap<>();
    for (ArticleFavoriteCount c : readService.articlesFavoriteCount(articleIds)) {
      found.put(c.getId(), c.getCount());
    }
    for (String id : articleIds) {
      result.add(new ArticleFavoriteCount(id, found.getOrDefault(id, 0)));
    }
    return result;
  }

  /** Subset of the given article ids that the user has favorited. */
  public List<String> status(String userId, List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return new ArrayList<>();
    }
    return readService.userFavorites(articleIds, userId);
  }

  public boolean isFavorite(String userId, String articleId) {
    return readService.isUserFavorite(userId, articleId);
  }

  public List<String> favoritedByUser(String userId) {
    return readService.articlesFavoritedByUser(userId);
  }
}
