package io.spring.core.service;

import io.spring.core.article.Article;

/**
 * Article write authorization keyed by the referenced author id: article-service authenticates from
 * the JWT subject (a user id) and never reads the {@code users} table.
 */
public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId != null && userId.equals(article.getUserId());
  }
}
