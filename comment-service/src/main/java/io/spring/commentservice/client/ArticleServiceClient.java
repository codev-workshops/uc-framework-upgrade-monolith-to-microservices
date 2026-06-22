package io.spring.commentservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${article-service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  public ArticleInfo getArticleBySlug(String slug) {
    String url = articleServiceUrl + "/internal/articles/by-slug/" + slug;
    return restTemplate.getForObject(url, ArticleInfo.class);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ArticleInfo {
    private String id;
    private String userId;
    private String slug;
  }
}
