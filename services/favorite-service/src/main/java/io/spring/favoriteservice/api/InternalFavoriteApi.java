package io.spring.favoriteservice.api;

import io.spring.favoriteservice.infrastructure.FavoriteCountRow;
import io.spring.favoriteservice.infrastructure.FavoritesReadService;
import io.spring.shared.data.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  private FavoritesReadService favoritesReadService;

  @GetMapping("/check")
  public ResponseEntity<Boolean> isUserFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleId") String articleId) {
    return ResponseEntity.ok(favoritesReadService.isUserFavorite(userId, articleId));
  }

  @GetMapping("/count/{articleId}")
  public ResponseEntity<Integer> favoriteCount(@PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(favoritesReadService.articleFavoriteCount(articleId));
  }

  @GetMapping("/counts")
  public ResponseEntity<List<ArticleFavoriteCount>> articlesFavoriteCount(
      @RequestParam("ids") List<String> ids) {
    List<FavoriteCountRow> rows = favoritesReadService.articlesFavoriteCount(ids);
    List<ArticleFavoriteCount> result =
        rows.stream()
            .map(row -> new ArticleFavoriteCount(row.getId(), row.getFavoriteCount()))
            .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }

  @GetMapping("/user-favorites")
  public ResponseEntity<Set<String>> userFavorites(
      @RequestParam("ids") List<String> ids, @RequestParam("userId") String userId) {
    return ResponseEntity.ok(favoritesReadService.userFavorites(ids, userId));
  }
}
