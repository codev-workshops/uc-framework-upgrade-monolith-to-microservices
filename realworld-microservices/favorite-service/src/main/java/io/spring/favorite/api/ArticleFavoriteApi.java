package io.spring.favorite.api;

import io.spring.favorite.api.exception.ResourceNotFoundException;
import io.spring.favorite.client.ArticleServiceClient;
import io.spring.favorite.core.ArticleFavorite;
import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.infrastructure.ArticleFavoritesReadService;
import io.spring.shared.auth.UserPrincipal;
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
    private final ArticleFavoritesReadService articleFavoritesReadService;

    public ArticleFavoriteApi(
            ArticleFavoriteRepository articleFavoriteRepository,
            ArticleServiceClient articleServiceClient,
            ArticleFavoritesReadService articleFavoritesReadService) {
        this.articleFavoriteRepository = articleFavoriteRepository;
        this.articleServiceClient = articleServiceClient;
        this.articleFavoritesReadService = articleFavoritesReadService;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> favoriteArticle(
            @PathVariable("slug") String slug, @AuthenticationPrincipal UserPrincipal user) {
        String articleId = articleServiceClient.getArticleIdBySlug(slug);
        if (articleId == null) {
            throw new ResourceNotFoundException();
        }
        ArticleFavorite articleFavorite = new ArticleFavorite(articleId, user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return responseFavoriteData(articleId, user.getId());
    }

    @DeleteMapping
    public ResponseEntity<HashMap<String, Object>> unfavoriteArticle(
            @PathVariable("slug") String slug, @AuthenticationPrincipal UserPrincipal user) {
        String articleId = articleServiceClient.getArticleIdBySlug(slug);
        if (articleId == null) {
            throw new ResourceNotFoundException();
        }
        articleFavoriteRepository
                .find(articleId, user.getId())
                .ifPresent(articleFavoriteRepository::remove);
        return responseFavoriteData(articleId, user.getId());
    }

    private ResponseEntity<HashMap<String, Object>> responseFavoriteData(String articleId, String userId) {
        boolean favorited = articleFavoritesReadService.isUserFavorite(userId, articleId);
        int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleId);
        HashMap<String, Object> response = new HashMap<>();
        response.put("favorited", favorited);
        response.put("favoritesCount", favoriteCount);
        return ResponseEntity.ok(response);
    }
}
