package io.spring.favorite.api;

import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.infrastructure.ArticleFavoritesReadService;
import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/favorites")
public class InternalFavoriteApi {
    private final ArticleFavoritesReadService articleFavoritesReadService;
    private final ArticleFavoriteRepository articleFavoriteRepository;

    public InternalFavoriteApi(
            ArticleFavoritesReadService articleFavoritesReadService,
            ArticleFavoriteRepository articleFavoriteRepository) {
        this.articleFavoritesReadService = articleFavoritesReadService;
        this.articleFavoriteRepository = articleFavoriteRepository;
    }

    @GetMapping("/count")
    public Map<String, Integer> getFavoriteCounts(@RequestParam("articleIds") List<String> articleIds) {
        List<ArticleFavoriteCount> counts = articleFavoritesReadService.articlesFavoriteCount(articleIds);
        Map<String, Integer> result = new HashMap<>();
        for (ArticleFavoriteCount count : counts) {
            result.put(count.getId(), count.getCount());
        }
        return result;
    }

    @GetMapping("/is-favorited")
    public boolean isFavorited(
            @RequestParam("articleId") String articleId,
            @RequestParam("userId") String userId) {
        return articleFavoritesReadService.isUserFavorite(userId, articleId);
    }

    @GetMapping("/user-favorites")
    public Set<String> getUserFavorites(
            @RequestParam("articleIds") List<String> articleIds,
            @RequestParam("userId") String userId) {
        return articleFavoritesReadService.userFavorites(articleIds, userId);
    }
}
