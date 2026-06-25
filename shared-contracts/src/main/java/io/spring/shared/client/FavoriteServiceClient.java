package io.spring.shared.client;

import io.spring.shared.data.ArticleFavoriteCount;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FavoriteServiceClient {

  private final RestTemplate restTemplate;
  private final String favoriteServiceUrl;

  public FavoriteServiceClient(
      RestTemplate restTemplate,
      @Value("${services.favorite-service.url:http://localhost:8084}") String favoriteServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoriteServiceUrl = favoriteServiceUrl;
  }

  public boolean isUserFavorite(String userId, String articleId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              favoriteServiceUrl + "/internal/favorites/check?userId={userId}&articleId={articleId}",
              Boolean.class,
              userId,
              articleId);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public int articleFavoriteCount(String articleId) {
    try {
      Integer result =
          restTemplate.getForObject(
              favoriteServiceUrl + "/internal/favorites/count/{articleId}",
              Integer.class,
              articleId);
      return result != null ? result : 0;
    } catch (Exception e) {
      return 0;
    }
  }

  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> articleIds) {
    String url =
        UriComponentsBuilder.fromHttpUrl(favoriteServiceUrl + "/internal/favorites/counts")
            .queryParam("ids", articleIds.toArray())
            .toUriString();
    List<ArticleFavoriteCount> result =
        restTemplate
            .exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ArticleFavoriteCount>>() {})
            .getBody();
    return result != null ? result : Collections.emptyList();
  }

  public Set<String> userFavorites(List<String> articleIds, String userId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(favoriteServiceUrl + "/internal/favorites/user-favorites")
            .queryParam("ids", articleIds.toArray())
            .queryParam("userId", userId)
            .toUriString();
    Set<String> result =
        restTemplate
            .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {})
            .getBody();
    return result != null ? result : Collections.emptySet();
  }
}
