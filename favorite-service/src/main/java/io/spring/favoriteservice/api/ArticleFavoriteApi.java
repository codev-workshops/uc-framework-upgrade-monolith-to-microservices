package io.spring.favoriteservice.api;

import io.spring.common.data.ArticleData;
import io.spring.common.exception.ResourceNotFoundException;
import io.spring.common.security.JwtUserPrincipal;
import io.spring.favoriteservice.application.ArticleQueryService;
import io.spring.favoriteservice.client.ArticleServiceClient;
import io.spring.favoriteservice.domain.ArticleFavorite;
import io.spring.favoriteservice.domain.ArticleFavoriteRepository;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
public class ArticleFavoriteApi {

  private final ArticleFavoriteRepository articleFavoriteRepository;
  private final ArticleServiceClient articleServiceClient;
  private final ArticleQueryService articleQueryService;

  public ArticleFavoriteApi(
      ArticleFavoriteRepository articleFavoriteRepository,
      ArticleServiceClient articleServiceClient,
      ArticleQueryService articleQueryService) {
    this.articleFavoriteRepository = articleFavoriteRepository;
    this.articleServiceClient = articleServiceClient;
    this.articleQueryService = articleQueryService;
  }

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal JwtUserPrincipal user) {
    ArticleData article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getUserId());
    articleFavoriteRepository.save(articleFavorite);
    return responseArticleData(
        articleQueryService.findBySlug(slug, user.getUserId()).get());
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal JwtUserPrincipal user) {
    ArticleData article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    articleFavoriteRepository
        .find(article.getId(), user.getUserId())
        .ifPresent(articleFavoriteRepository::remove);
    return responseArticleData(
        articleQueryService.findBySlug(slug, user.getUserId()).get());
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
