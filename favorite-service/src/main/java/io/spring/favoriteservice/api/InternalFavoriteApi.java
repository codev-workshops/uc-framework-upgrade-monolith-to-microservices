package io.spring.favoriteservice.api;

import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoriteCountResult;
import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/favorites")
@AllArgsConstructor
public class InternalFavoriteApi {

  private ArticleFavoritesReadService articleFavoritesReadService;

  @GetMapping("/count/{articleId}")
  public int getFavoriteCount(@PathVariable("articleId") String articleId) {
    return articleFavoritesReadService.articleFavoriteCount(articleId);
  }

  @GetMapping("/is-favorited")
  public boolean isUserFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleId") String articleId) {
    return articleFavoritesReadService.isUserFavorite(userId, articleId);
  }

  @GetMapping("/counts")
  public List<ArticleFavoriteCount> getFavoriteCounts(
      @RequestParam("articleIds") List<String> articleIds) {
    List<ArticleFavoriteCountResult> results =
        articleFavoritesReadService.articlesFavoriteCount(articleIds);
    return results.stream()
        .map(r -> new ArticleFavoriteCount(r.getId(), r.getFavoriteCount()))
        .collect(Collectors.toList());
  }

  @GetMapping("/user-favorites")
  public Set<String> getUserFavorites(
      @RequestParam("articleIds") List<String> articleIds,
      @RequestParam("userId") String userId) {
    return articleFavoritesReadService.userFavorites(articleIds, userId);
  }
}
