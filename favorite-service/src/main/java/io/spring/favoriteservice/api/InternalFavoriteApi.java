package io.spring.favoriteservice.api;

import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/favorites")
@AllArgsConstructor
public class InternalFavoriteApi {

  private ArticleFavoritesReadService articleFavoritesReadService;

  @GetMapping("/count")
  public ResponseEntity<Map<String, Integer>> getFavoriteCounts(
      @RequestParam("articleIds") List<String> articleIds) {
    Map<String, Integer> countMap = new HashMap<>();
    articleIds.forEach(id -> countMap.put(id, articleFavoritesReadService.articleFavoriteCount(id)));
    return ResponseEntity.ok(countMap);
  }

  @GetMapping("/is-favorited")
  public ResponseEntity<Set<String>> isUserFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleIds") List<String> articleIds) {
    return ResponseEntity.ok(articleFavoritesReadService.userFavorites(userId, articleIds));
  }

  @GetMapping("/{articleId}/count")
  public ResponseEntity<Integer> articleFavoriteCount(@PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(articleFavoritesReadService.articleFavoriteCount(articleId));
  }

  @GetMapping("/{userId}/{articleId}/is-favorited")
  public ResponseEntity<Boolean> isUserFavoriteSingle(
      @PathVariable("userId") String userId, @PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(articleFavoritesReadService.isUserFavorite(userId, articleId));
  }
}
