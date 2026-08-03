package io.spring.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Delegates the public {@code /articles/{slug}/comments} routes to comment-service internal
 * endpoints (keyed by the referenced {@code articleId}), returning the exact RealWorld comment
 * shapes unchanged.
 */
@Component
public class CommentGateway {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public CommentGateway(
      RestTemplate restTemplate, @Value("${services.comment.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public JsonNode findByArticleId(String articleId, String currentUserId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/articles/" + articleId + "/comments")
            .queryParamIfPresent("currentUserId", java.util.Optional.ofNullable(currentUserId))
            .toUriString();
    return restTemplate.getForObject(url, JsonNode.class);
  }

  public JsonNode createComment(String articleId, String body, String userId) {
    return restTemplate.postForObject(
        baseUrl + "/internal/articles/" + articleId + "/comments",
        Map.of("body", body, "userId", userId),
        JsonNode.class);
  }

  public void deleteComment(String commentId) {
    restTemplate.delete(baseUrl + "/internal/comments/" + commentId);
  }
}
