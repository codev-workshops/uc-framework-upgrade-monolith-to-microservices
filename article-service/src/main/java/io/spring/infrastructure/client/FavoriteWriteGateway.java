package io.spring.infrastructure.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Write side of favorite-service used by the public {@code /articles/{slug}/favorite} routes, keyed
 * by referenced ids ({@code articleId}, {@code userId}).
 */
@Component
public class FavoriteWriteGateway {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public FavoriteWriteGateway(
      RestTemplate restTemplate, @Value("${services.favorite.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public void favorite(String articleId, String userId) {
    restTemplate.postForObject(
        baseUrl + "/internal/favorites",
        Map.of("articleId", articleId, "userId", userId),
        Void.class);
  }

  public void unfavorite(String articleId, String userId) {
    restTemplate.exchange(
        baseUrl + "/internal/favorites",
        HttpMethod.DELETE,
        new HttpEntity<>(Map.of("articleId", articleId, "userId", userId)),
        Void.class);
  }
}
