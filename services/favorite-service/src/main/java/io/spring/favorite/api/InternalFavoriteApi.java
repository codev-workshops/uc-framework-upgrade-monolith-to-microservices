package io.spring.favorite.api;

import io.spring.favorite.api.exception.ResourceNotFoundException;
import io.spring.favorite.application.FavoriteService;
import io.spring.favorite.application.data.ArticleFavoriteCount;
import io.spring.favorite.client.UserAuthClient;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/favorites")
public class InternalFavoriteApi {
  private final FavoriteService favoriteService;
  private final UserAuthClient userAuthClient;

  public InternalFavoriteApi(FavoriteService favoriteService, UserAuthClient userAuthClient) {
    this.favoriteService = favoriteService;
    this.userAuthClient = userAuthClient;
  }

  @PostMapping("/counts")
  public ResponseEntity<List<ArticleFavoriteCount>> counts(@RequestBody List<String> articleIds) {
    return ResponseEntity.ok(favoriteService.counts(articleIds));
  }

  @GetMapping("/count/{articleId}")
  public ResponseEntity<Map<String, Object>> count(@PathVariable("articleId") String articleId) {
    Map<String, Object> body = new java.util.HashMap<>();
    body.put("id", articleId);
    body.put("count", favoriteService.count(articleId));
    return ResponseEntity.ok(body);
  }

  @PostMapping("/status")
  public ResponseEntity<List<String>> status(
      @RequestParam("userId") String userId, @RequestBody List<String> articleIds) {
    return ResponseEntity.ok(favoriteService.status(userId, articleIds));
  }

  @GetMapping("/is-favorite")
  public ResponseEntity<Map<String, Object>> isFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleId") String articleId) {
    return ResponseEntity.ok(
        Collections.singletonMap("favorited", favoriteService.isFavorite(userId, articleId)));
  }

  @GetMapping("/by-user")
  public ResponseEntity<List<String>> byUser(
      @RequestParam(value = "userId", required = false) String userId,
      @RequestParam(value = "username", required = false) String username) {
    String resolvedUserId = resolveUserId(userId, username);
    return ResponseEntity.ok(favoriteService.favoritedByUser(resolvedUserId));
  }

  private String resolveUserId(String userId, String username) {
    if (StringUtils.hasText(userId)) {
      return userId;
    }
    if (StringUtils.hasText(username)) {
      return userAuthClient
          .findUserIdByUsername(username)
          .orElseThrow(ResourceNotFoundException::new);
    }
    throw new ResourceNotFoundException();
  }
}
