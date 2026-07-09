package io.spring.core.service;

import io.spring.core.user.User;

/**
 * Resource-ownership authorization contract owned by user-auth-service. The concrete article /
 * comment domain objects live in their owning services, so this service exposes the ownership rules
 * in terms of the owner user ids that those services resolve from their own stores.
 */
public class AuthorizationService {
  public static boolean canWriteArticle(User user, String articleAuthorId) {
    return user.getId().equals(articleAuthorId);
  }

  public static boolean canWriteComment(
      User user, String articleAuthorId, String commentAuthorId) {
    return user.getId().equals(articleAuthorId) || user.getId().equals(commentAuthorId);
  }
}
