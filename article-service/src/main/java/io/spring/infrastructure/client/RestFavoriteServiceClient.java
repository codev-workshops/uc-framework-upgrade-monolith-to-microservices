package io.spring.infrastructure.client;

import io.spring.contracts.client.FavoriteServiceClient;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Resolves favorite counts and per-user favorite state from favorite-service over HTTP. */
@Component
public class RestFavoriteServiceClient implements FavoriteServiceClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestFavoriteServiceClient(
      RestTemplate restTemplate, @Value("${services.favorite.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/favorites/count")
            .queryParam("articleId", articleId)
            .toUriString();
    try {
      Integer count = restTemplate.getForObject(url, Integer.class);
      return count == null ? 0 : count;
    } catch (RuntimeException e) {
      return 0;
    }
  }

  @Override
  public Map<String, Integer> favoriteCounts(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Map.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/favorites/counts")
            .queryParam("articleIds", String.join(",", articleIds))
            .toUriString();
    try {
      Map<String, Integer> counts =
          restTemplate
              .exchange(
                  url,
                  HttpMethod.GET,
                  null,
                  new ParameterizedTypeReference<Map<String, Integer>>() {})
              .getBody();
      return counts == null ? Map.of() : counts;
    } catch (RuntimeException e) {
      return Map.of();
    }
  }

  @Override
  public boolean isFavorited(String userId, String articleId) {
    if (userId == null || articleId == null) {
      return false;
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/favorites/favorited")
            .queryParam("userId", userId)
            .queryParam("articleId", articleId)
            .toUriString();
    try {
      return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public Set<String> userFavorites(List<String> articleIds, String userId) {
    if (userId == null || articleIds == null || articleIds.isEmpty()) {
      return Set.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/favorites/user-favorites")
            .queryParam("articleIds", String.join(",", articleIds))
            .queryParam("userId", userId)
            .toUriString();
    try {
      Set<String> favorites =
          restTemplate
              .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {})
              .getBody();
      return favorites == null ? Set.of() : favorites;
    } catch (RuntimeException e) {
      return Set.of();
    }
  }
}
