package io.spring.favoriteservice.infrastructure.client;

import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.dto.ArticleData;
import io.spring.shared.exception.ServiceUnavailableException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateArticleServiceClient implements ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public RestTemplateArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${services.article-service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  @Override
  public Optional<ArticleData> getArticleBySlug(String slug) {
    try {
      ResponseEntity<ArticleData> response =
          restTemplate.getForEntity(
              articleServiceUrl + "/internal/articles/slug/{slug}", ArticleData.class, slug);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("Article Service");
    }
  }

  @Override
  public Optional<ArticleData> getArticleById(String id) {
    try {
      ResponseEntity<ArticleData> response =
          restTemplate.getForEntity(
              articleServiceUrl + "/internal/articles/{id}", ArticleData.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("Article Service");
    }
  }
}
