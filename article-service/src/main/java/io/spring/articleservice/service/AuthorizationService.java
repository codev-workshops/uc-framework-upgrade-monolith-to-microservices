package io.spring.articleservice.service;

import io.spring.articleservice.domain.Article;

public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId.equals(article.getUserId());
  }
}
