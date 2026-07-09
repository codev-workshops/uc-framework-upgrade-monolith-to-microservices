package io.spring.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Authenticated principal for comment-service. Only the user id (JWT {@code sub}) is known locally;
 * the author projection is resolved from user-auth-service when needed.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class CurrentUser {
  private final String id;
}
