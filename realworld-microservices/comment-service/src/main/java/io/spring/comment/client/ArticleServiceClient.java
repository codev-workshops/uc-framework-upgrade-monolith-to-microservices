package io.spring.comment.client;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {
  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      @Value("${article-service.url:http://localhost:8083}") String url) {
    this.restTemplate = new RestTemplate();
    this.articleServiceUrl = url;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getArticleBySlug(String slug) {
    String url = articleServiceUrl + "/internal/articles/by-slug/" + slug;
    try {
      Map<String, String> result = restTemplate.getForObject(url, Map.class);
      if (result != null) {
        return result;
      }
    } catch (Exception e) {
      // fallback to empty
    }
    return Collections.emptyMap();
  }
}
