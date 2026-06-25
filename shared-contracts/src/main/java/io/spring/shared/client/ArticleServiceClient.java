package io.spring.shared.client;

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

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${services.article-service.url:http://localhost:8082}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  @SuppressWarnings("unchecked")
  public Optional<Map<String, Object>> findArticleBySlug(String slug) {
    try {
      Map<String, Object> article =
          restTemplate.getForObject(
              articleServiceUrl + "/internal/articles/by-slug/{slug}", Map.class, slug);
      return Optional.ofNullable(article);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public Optional<Map<String, Object>> findArticleById(String articleId) {
    try {
      Map<String, Object> article =
          restTemplate.getForObject(
              articleServiceUrl + "/internal/articles/{id}", Map.class, articleId);
      return Optional.ofNullable(article);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
