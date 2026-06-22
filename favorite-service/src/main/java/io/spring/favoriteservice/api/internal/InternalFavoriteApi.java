package io.spring.favoriteservice.api.internal;

import io.spring.common.data.ArticleFavoriteCount;
import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/favorites")
public class InternalFavoriteApi {

  private final ArticleFavoritesReadService articleFavoritesReadService;

  public InternalFavoriteApi(ArticleFavoritesReadService articleFavoritesReadService) {
    this.articleFavoritesReadService = articleFavoritesReadService;
  }

  @GetMapping("/counts")
  public Map<String, Integer> getFavoriteCounts(@RequestParam("articleIds") List<String> articleIds) {
    List<ArticleFavoriteCount> counts = articleFavoritesReadService.articlesFavoriteCount(articleIds);
    Map<String, Integer> result = new HashMap<>();
    for (ArticleFavoriteCount count : counts) {
      result.put(count.getId(), count.getCount());
    }
    return result;
  }

  @GetMapping("/article/{articleId}/count")
  public int getArticleFavoriteCount(@PathVariable("articleId") String articleId) {
    return articleFavoritesReadService.articleFavoriteCount(articleId);
  }

  @GetMapping("/user/{userId}/check")
  public boolean hasUserFavorited(
      @PathVariable("userId") String userId, @RequestParam("articleId") String articleId) {
    return articleFavoritesReadService.isUserFavorite(userId, articleId);
  }

  @GetMapping("/user/{userId}")
  public Set<String> getUserFavorites(
      @PathVariable("userId") String userId, @RequestParam("articleIds") List<String> articleIds) {
    return articleFavoritesReadService.userFavorites(articleIds, userId);
  }
}
