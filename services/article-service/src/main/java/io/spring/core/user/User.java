package io.spring.core.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Lightweight principal for the article-service. Only the user id (JWT {@code sub}) is available
 * locally; the {@code users} table lives in user-auth-service and is resolved via the network when
 * author details are needed.
 */
@Getter
@EqualsAndHashCode(of = {"id"})
public class User {
  private final String id;

  public User(String id) {
    this.id = id;
  }
}
