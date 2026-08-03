package io.spring.infrastructure.client;

import io.spring.application.CommentQueryService;
import io.spring.contracts.client.CommentServiceClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/** In-process implementation of the contract this service owns; exposed over REST by the API. */
@Component
@AllArgsConstructor
public class LocalCommentServiceClient implements CommentServiceClient {
  private final CommentQueryService commentQueryService;

  @Override
  public int commentCount(String articleId) {
    return commentQueryService.countByArticleId(articleId);
  }
}
