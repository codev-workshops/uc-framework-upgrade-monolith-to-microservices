package io.spring.core.service;

import io.spring.application.data.ArticleRef;
import io.spring.core.comment.Comment;

/**
 * Reimplements the monolith {@code AuthorizationService.canWriteComment} semantics. The article
 * author id comes from the article-service contract ({@link ArticleRef#getAuthorId()}); the comment
 * author id comes from comment-service's own store.
 */
public class AuthorizationService {
  public static boolean canWriteComment(String userId, ArticleRef article, Comment comment) {
    return userId.equals(article.getAuthorId()) || userId.equals(comment.getUserId());
  }
}
