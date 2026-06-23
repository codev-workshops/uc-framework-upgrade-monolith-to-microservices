package io.spring.articleservice.infrastructure;

import io.spring.shared.client.FavoriteServiceClient;
import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFavoriteServiceClient implements FavoriteServiceClient {

  private final RestTemplate restTemplate;
  private final String favoriteServiceUrl;

  public RestTemplateFavoriteServiceClient(
      RestTemplate restTemplate,
      @Value("${services.favorite-service.url}") String favoriteServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoriteServiceUrl = favoriteServiceUrl;
  }

  @Override
  public int getFavoriteCount(String articleId) {
    try {
      String url = favoriteServiceUrl + "/internal/favorites/count?articleId=" + articleId;
      ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
      return response.getBody() != null ? response.getBody() : 0;
    } catch (RestClientException e) {
      return 0;
    }
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    try {
      String url =
          favoriteServiceUrl
              + "/internal/favorites/check?userId="
              + userId
              + "&articleId="
              + articleId;
      ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
      return response.getBody() != null && response.getBody();
    } catch (RestClientException e) {
      return false;
    }
  }

  @Override
  public List<ArticleFavoriteCount> getFavoriteCounts(List<String> articleIds) {
    try {
      String ids = String.join(",", articleIds);
      String url = favoriteServiceUrl + "/internal/favorites/counts?articleIds=" + ids;
      ResponseEntity<List<ArticleFavoriteCount>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ArticleFavoriteCount>>() {});
      return response.getBody() != null ? response.getBody() : Collections.emptyList();
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public Set<String> getUserFavorites(List<String> articleIds, String userId) {
    try {
      String ids = String.join(",", articleIds);
      String url =
          favoriteServiceUrl
              + "/internal/favorites/user-favorites?articleIds="
              + ids
              + "&userId="
              + userId;
      ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);
      if (response.getBody() != null) {
        return new HashSet<>(Arrays.asList(response.getBody()));
      }
      return Collections.emptySet();
    } catch (RestClientException e) {
      return Collections.emptySet();
    }
  }
}
