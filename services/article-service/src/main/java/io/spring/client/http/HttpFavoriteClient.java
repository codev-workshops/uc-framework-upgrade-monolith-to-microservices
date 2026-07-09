package io.spring.client.http;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.client.FavoriteClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("!mock")
public class HttpFavoriteClient implements FavoriteClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpFavoriteClient(
      RestTemplate upstreamRestTemplate, @Value("${services.favorite.url}") String baseUrl) {
    this.restTemplate = upstreamRestTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public List<ArticleFavoriteCount> countsForArticles(List<String> articleIds) {
    if (articleIds.isEmpty()) {
      return Collections.emptyList();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl).path("/internal/favorites/counts").toUriString();
    HttpEntity<List<String>> entity = new HttpEntity<>(articleIds, ClientConfig.jsonHeaders());
    ArticleFavoriteCount[] body =
        restTemplate.exchange(url, HttpMethod.POST, entity, ArticleFavoriteCount[].class).getBody();
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }

  @Override
  public List<String> favoritedArticles(String userId, List<String> articleIds) {
    if (articleIds.isEmpty()) {
      return Collections.emptyList();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/internal/favorites/status")
            .queryParam("userId", userId)
            .toUriString();
    HttpEntity<List<String>> entity = new HttpEntity<>(articleIds, ClientConfig.jsonHeaders());
    String[] body = restTemplate.exchange(url, HttpMethod.POST, entity, String[].class).getBody();
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }

  @Override
  public List<String> articlesFavoritedByUser(String userId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/internal/favorites/by-user")
            .queryParam("userId", userId)
            .toUriString();
    String[] body = restTemplate.getForObject(url, String[].class);
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }
}
