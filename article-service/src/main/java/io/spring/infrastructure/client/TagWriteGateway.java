package io.spring.infrastructure.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Write side of tag-service: on article create/delete, article-service pushes the {@code tagList}
 * to tag-service (which owns the {@code tags} / {@code article_tags} tables) keyed by the
 * referenced {@code articleId}.
 */
@Component
public class TagWriteGateway {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public TagWriteGateway(RestTemplate restTemplate, @Value("${services.tag.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public void setArticleTags(String articleId, List<String> tags) {
    if (tags == null) {
      return;
    }
    try {
      restTemplate.exchange(
          baseUrl + "/internal/articles/" + articleId + "/tags",
          HttpMethod.PUT,
          new HttpEntity<>(tags),
          Void.class);
    } catch (RuntimeException ignored) {
      // tag persistence is best-effort composition; article write still succeeds.
    }
  }

  public void removeArticleTags(String articleId) {
    try {
      restTemplate.delete(baseUrl + "/internal/articles/" + articleId + "/tags");
    } catch (RuntimeException ignored) {
      // best-effort cleanup
    }
  }
}
