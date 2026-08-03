package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.client.FavoriteWriteGateway;
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
  private final FavoriteWriteGateway favoriteWriteGateway;
  private final ArticleRepository articleRepository;
  private final ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity<?> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    favoriteWriteGateway.favorite(article.getId(), userId);
    return responseArticleData(articleQueryService.findBySlug(slug, userId).get());
  }

  @DeleteMapping
  public ResponseEntity<?> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    favoriteWriteGateway.unfavorite(article.getId(), userId);
    return responseArticleData(articleQueryService.findBySlug(slug, userId).get());
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
