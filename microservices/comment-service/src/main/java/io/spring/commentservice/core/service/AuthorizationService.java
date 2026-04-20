package io.spring.commentservice.core.service;

import io.spring.commentservice.core.comment.Comment;
import io.spring.shared.auth.UserPrincipal;

public class AuthorizationService {
  public static boolean canWriteComment(UserPrincipal userPrincipal, Comment comment) {
    return userPrincipal.getId().equals(comment.getUserId());
  }
}
