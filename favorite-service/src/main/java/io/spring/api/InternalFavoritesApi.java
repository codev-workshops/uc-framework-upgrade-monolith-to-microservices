package io.spring.api;

import io.spring.application.FavoriteQueryService;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inter-service endpoints backing the {@code FavoriteServiceClient} contract plus internal write
 * endpoints, so other services resolve favorite state without reading the {@code article_favorites}
 * table. All operations are keyed by referenced IDs ({@code articleId}, {@code userId}).
 */
@RestController
@RequestMapping(path = "/internal/favorites")
@AllArgsConstructor
public class InternalFavoritesApi {

  private final FavoriteQueryService favoriteQueryService;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  @GetMapping("/count")
  public ResponseEntity<Integer> articleFavoriteCount(@RequestParam("articleId") String articleId) {
    return ResponseEntity.ok(favoriteQueryService.articleFavoriteCount(articleId));
  }

  @GetMapping("/counts")
  public ResponseEntity<Map<String, Integer>> favoriteCounts(
      @RequestParam("articleIds") List<String> articleIds) {
    return ResponseEntity.ok(favoriteQueryService.favoriteCounts(articleIds));
  }

  @GetMapping("/favorited")
  public ResponseEntity<Boolean> isFavorited(
      @RequestParam("userId") String userId, @RequestParam("articleId") String articleId) {
    return ResponseEntity.ok(favoriteQueryService.isFavorited(userId, articleId));
  }

  @GetMapping("/user-favorites")
  public ResponseEntity<Set<String>> userFavorites(
      @RequestParam("articleIds") List<String> articleIds, @RequestParam("userId") String userId) {
    return ResponseEntity.ok(favoriteQueryService.userFavorites(articleIds, userId));
  }

  @PostMapping
  public ResponseEntity<Void> favorite(@RequestBody FavoriteRequest request) {
    articleFavoriteRepository.save(
        new ArticleFavorite(request.getArticleId(), request.getUserId()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> unfavorite(@RequestBody FavoriteRequest request) {
    articleFavoriteRepository
        .find(request.getArticleId(), request.getUserId())
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.noContent().build();
  }
}
