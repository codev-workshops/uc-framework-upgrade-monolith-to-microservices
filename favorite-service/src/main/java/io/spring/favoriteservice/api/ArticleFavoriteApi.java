package io.spring.favoriteservice.api;

import io.spring.common.client.ArticleServiceClient;
import io.spring.common.dto.ArticleIdResponse;
import io.spring.favoriteservice.api.exception.ResourceNotFoundException;
import io.spring.favoriteservice.core.ArticleFavorite;
import io.spring.favoriteservice.core.ArticleFavoriteRepository;
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

  private ArticleServiceClient articleServiceClient;
  private ArticleFavoriteRepository articleFavoriteRepository;

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleIdResponse article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(favorite);
    return ResponseEntity.ok(
        favoriteResponse(article.getId(), userId));
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleIdResponse article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.ok(
        favoriteResponse(article.getId(), userId));
  }

  private Map<String, Object> favoriteResponse(String articleId, String userId) {
    int count = articleFavoriteRepository.countByArticleId(articleId);
    boolean favorited = articleFavoriteRepository.find(articleId, userId).isPresent();
    return new HashMap<String, Object>() {
      {
        put("article", new HashMap<String, Object>() {
          {
            put("favoritesCount", count);
            put("favorited", favorited);
          }
        });
      }
    };
  }
}
