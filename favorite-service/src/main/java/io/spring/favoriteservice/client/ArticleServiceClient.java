package io.spring.favoriteservice.client;

import io.spring.common.data.ArticleData;
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

  public ArticleData getArticleBySlug(String slug) {
    return restTemplate.getForObject(
        articleServiceUrl + "/internal/articles/by-slug/{slug}", ArticleData.class, slug);
  }

  public ArticleData getArticleById(String id) {
    return restTemplate.getForObject(
        articleServiceUrl + "/internal/articles/{id}", ArticleData.class, id);
  }
}
