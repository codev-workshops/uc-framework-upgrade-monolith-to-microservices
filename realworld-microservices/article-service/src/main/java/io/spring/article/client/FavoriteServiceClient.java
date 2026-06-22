package io.spring.article.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FavoriteServiceClient {
  private final RestTemplate restTemplate;
  private final String favoriteServiceUrl;

  public FavoriteServiceClient(
      @Value("${favorite-service.url:http://localhost:8084}") String url) {
    this.restTemplate = new RestTemplate();
    this.favoriteServiceUrl = url;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Integer> getFavoriteCounts(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Collections.emptyMap();
    }
    String ids = String.join(",", articleIds);
    String url = favoriteServiceUrl + "/internal/favorites/count?articleIds=" + ids;
    try {
      Map<String, Integer> result = restTemplate.getForObject(url, Map.class);
      if (result != null) {
        return result;
      }
    } catch (Exception e) {
      // fallback to empty
    }
    return Collections.emptyMap();
  }

  public boolean isFavorited(String articleId, String userId) {
    String url =
        favoriteServiceUrl
            + "/internal/favorites/is-favorited?articleId="
            + articleId
            + "&userId="
            + userId;
    try {
      Boolean result = restTemplate.getForObject(url, Boolean.class);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> getUserFavorites(List<String> articleIds, String userId) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Collections.emptySet();
    }
    String ids = String.join(",", articleIds);
    String url =
        favoriteServiceUrl
            + "/internal/favorites/user-favorites?articleIds="
            + ids
            + "&userId="
            + userId;
    try {
      String[] result = restTemplate.getForObject(url, String[].class);
      if (result != null) {
        return new HashSet<>(Arrays.asList(result));
      }
    } catch (Exception e) {
      // fallback to empty
    }
    return Collections.emptySet();
  }
}
