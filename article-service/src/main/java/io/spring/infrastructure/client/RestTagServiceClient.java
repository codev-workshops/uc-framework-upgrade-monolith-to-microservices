package io.spring.infrastructure.client;

import io.spring.contracts.client.TagServiceClient;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Resolves article tag lists from tag-service over HTTP for read composition. */
@Component
public class RestTagServiceClient implements TagServiceClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestTagServiceClient(
      RestTemplate restTemplate, @Value("${services.tag.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public List<String> allTags() {
    try {
      List<String> tags =
          restTemplate
              .exchange(
                  baseUrl + "/internal/tags",
                  HttpMethod.GET,
                  null,
                  new ParameterizedTypeReference<List<String>>() {})
              .getBody();
      return tags == null ? List.of() : tags;
    } catch (RuntimeException e) {
      return List.of();
    }
  }

  @Override
  public List<String> tagsOfArticle(String articleId) {
    try {
      List<String> tags =
          restTemplate
              .exchange(
                  baseUrl + "/internal/articles/" + articleId + "/tags",
                  HttpMethod.GET,
                  null,
                  new ParameterizedTypeReference<List<String>>() {})
              .getBody();
      return tags == null ? List.of() : tags;
    } catch (RuntimeException e) {
      return List.of();
    }
  }

  @Override
  public Map<String, List<String>> tagsOfArticles(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Map.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/article-tags")
            .queryParam("articleIds", String.join(",", articleIds))
            .toUriString();
    try {
      Map<String, List<String>> result =
          restTemplate
              .exchange(
                  url,
                  HttpMethod.GET,
                  null,
                  new ParameterizedTypeReference<Map<String, List<String>>>() {})
              .getBody();
      return result == null ? Map.of() : result;
    } catch (RuntimeException e) {
      return Map.of();
    }
  }
}
