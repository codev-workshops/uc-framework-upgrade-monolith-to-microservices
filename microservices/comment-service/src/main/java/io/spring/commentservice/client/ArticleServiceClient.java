package io.spring.commentservice.client;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(@Value("${article-service.url}") String articleServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.articleServiceUrl = articleServiceUrl;
  }

  @SuppressWarnings("unchecked")
  public Optional<String> getArticleIdBySlug(String slug) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              articleServiceUrl + "/internal/articles/{slug}", Map.class, slug);
      if (response != null && response.containsKey("id")) {
        return Optional.of((String) response.get("id"));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
