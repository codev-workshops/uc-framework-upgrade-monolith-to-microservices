package io.spring.commentservice.application;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {
  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate, @Value("${article-service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  public Optional<ArticleInfo> findBySlug(String slug) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              articleServiceUrl + "/internal/articles/by-slug/{slug}", Map.class, slug);
      if (response.getBody() != null) {
        Map body = response.getBody();
        String id = (String) body.get("id");
        String articleSlug = (String) body.get("slug");
        String userId = (String) body.get("userId");
        return Optional.of(new ArticleInfo(id, articleSlug, userId));
      }
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @lombok.Data
  @lombok.AllArgsConstructor
  public static class ArticleInfo {
    private String id;
    private String slug;
    private String userId;
  }
}
