package io.spring.infrastructure.clients;

import io.spring.application.clients.ArticleClient;
import io.spring.application.data.ArticleRef;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("!mock")
public class HttpArticleClient implements ArticleClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpArticleClient(
      RestTemplate restTemplate, @Value("${services.article.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<ArticleRef> findBySlug(String slug, String authorization) {
    try {
      HttpEntity<Void> request = new HttpEntity<>(ClientHeaders.forwarding(authorization));
      ResponseEntity<ArticleRef> response =
          restTemplate.exchange(
              baseUrl + "/internal/articles/{slug}",
              HttpMethod.GET,
              request,
              ArticleRef.class,
              slug);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
