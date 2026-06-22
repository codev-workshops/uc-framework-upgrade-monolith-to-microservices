package io.spring.articleservice.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FavoriteServiceClient {

  private final RestTemplate restTemplate;
  private final String favoriteServiceUrl;

  public FavoriteServiceClient(
      RestTemplate restTemplate, @Value("${favorite-service.url}") String favoriteServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoriteServiceUrl = favoriteServiceUrl;
  }

  public Map<String, Integer> getArticleFavoriteCounts(List<String> articleIds) {
    try {
      String articleIdsParam = String.join(",", articleIds);
      ResponseEntity<Map<String, Integer>> response =
          restTemplate.exchange(
              favoriteServiceUrl + "/internal/favorites/counts?articleIds={articleIds}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Integer>>() {},
              articleIdsParam);
      return response.getBody() != null ? response.getBody() : Collections.emptyMap();
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  public int getArticleFavoriteCount(String articleId) {
    try {
      Integer result =
          restTemplate.getForObject(
              favoriteServiceUrl + "/internal/favorites/article/{articleId}/count",
              Integer.class,
              articleId);
      return result != null ? result : 0;
    } catch (Exception e) {
      return 0;
    }
  }

  public boolean isUserFavorite(String userId, String articleId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              favoriteServiceUrl
                  + "/internal/favorites/user/{userId}/check?articleId={articleId}",
              Boolean.class,
              userId,
              articleId);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> getUserFavorites(List<String> articleIds, String userId) {
    try {
      String articleIdsParam = String.join(",", articleIds);
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              favoriteServiceUrl
                  + "/internal/favorites/user/{userId}?articleIds={articleIds}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Set<String>>() {},
              userId,
              articleIdsParam);
      return response.getBody() != null ? response.getBody() : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }
}
