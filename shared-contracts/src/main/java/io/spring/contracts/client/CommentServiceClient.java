package io.spring.contracts.client;

/**
 * Contract for comment counts/associations from comment-service. Kept minimal; comment bodies and
 * author enrichment are served directly by comment-service over its own REST API.
 */
public interface CommentServiceClient {
  int commentCount(String articleId);
}
