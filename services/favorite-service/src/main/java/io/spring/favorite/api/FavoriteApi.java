package io.spring.favorite.api;

import io.spring.favorite.api.exception.InvalidAuthenticationException;
import io.spring.favorite.application.FavoriteService;
import io.spring.favorite.application.data.FavoriteState;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{articleId}/favorite")
public class FavoriteApi {
  private final FavoriteService favoriteService;

  public FavoriteApi(FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }

  @PostMapping
  public ResponseEntity<FavoriteState> favoriteArticle(
      @PathVariable("articleId") String articleId, @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(favoriteService.favorite(articleId, currentUser(userId)));
  }

  @DeleteMapping
  public ResponseEntity<FavoriteState> unfavoriteArticle(
      @PathVariable("articleId") String articleId, @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(favoriteService.unfavorite(articleId, currentUser(userId)));
  }

  private String currentUser(String userId) {
    if (userId == null) {
      throw new InvalidAuthenticationException();
    }
    return userId;
  }
}
