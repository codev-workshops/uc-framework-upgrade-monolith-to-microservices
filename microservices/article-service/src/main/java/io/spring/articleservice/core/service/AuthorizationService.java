package io.spring.articleservice.core.service;

import io.spring.articleservice.core.article.Article;
import io.spring.shared.auth.UserPrincipal;

public class AuthorizationService {
  public static boolean canWriteArticle(UserPrincipal user, Article article) {
    return user.getId().equals(article.getUserId());
  }
}
