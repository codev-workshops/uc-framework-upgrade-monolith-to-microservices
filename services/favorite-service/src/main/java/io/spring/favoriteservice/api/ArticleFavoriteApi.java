package io.spring.favoriteservice.api;

import io.spring.favoriteservice.domain.ArticleFavorite;
import io.spring.favoriteservice.domain.ArticleFavoriteRepository;
import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.data.UserData;
import io.spring.shared.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleServiceClient articleServiceClient;

  @PostMapping
  public ResponseEntity favorite(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserData user) {
    Map<String, Object> article =
        articleServiceClient.findArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String articleId = (String) article.get("id");
    ArticleFavorite favorite = new ArticleFavorite(articleId, user.getId());
    articleFavoriteRepository.save(favorite);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", article);
          }
        });
  }

  @DeleteMapping
  public ResponseEntity unfavorite(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserData user) {
    Map<String, Object> article =
        articleServiceClient.findArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String articleId = (String) article.get("id");
    articleFavoriteRepository
        .find(articleId, user.getId())
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", article);
          }
        });
  }
}
