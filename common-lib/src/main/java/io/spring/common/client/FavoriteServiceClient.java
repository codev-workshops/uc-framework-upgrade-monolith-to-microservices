package io.spring.common.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "favorite-service",
    url = "${services.favorite-service.url:http://localhost:8084}")
public interface FavoriteServiceClient {

  @GetMapping("/internal/favorites/count")
  Map<String, Integer> getFavoriteCounts(@RequestParam("articleIds") List<String> articleIds);

  @GetMapping("/internal/favorites/is-favorited")
  Set<String> isUserFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleIds") List<String> articleIds);

  @GetMapping("/internal/favorites/{articleId}/count")
  int articleFavoriteCount(@PathVariable("articleId") String articleId);

  @GetMapping("/internal/favorites/{userId}/{articleId}/is-favorited")
  boolean isUserFavoriteSingle(
      @PathVariable("userId") String userId, @PathVariable("articleId") String articleId);
}
