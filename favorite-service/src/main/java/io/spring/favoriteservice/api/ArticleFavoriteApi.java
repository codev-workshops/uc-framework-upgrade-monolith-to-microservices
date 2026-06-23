package io.spring.favoriteservice.api;

import io.spring.favoriteservice.core.ArticleFavorite;
import io.spring.favoriteservice.core.ArticleFavoriteRepository;
import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.dto.ArticleData;
import io.spring.shared.exception.ResourceNotFoundException;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleServiceClient articleServiceClient;

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleData article =
        articleServiceClient.getArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(articleFavorite);
    return responseArticleData(
        articleServiceClient.getArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new));
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleData article =
        articleServiceClient.getArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));
    return responseArticleData(
        articleServiceClient.getArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new));
  }

  private ResponseEntity<HashMap<String, Object>> responseArticleData(
      final ArticleData articleData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleData);
          }
        });
  }
}
