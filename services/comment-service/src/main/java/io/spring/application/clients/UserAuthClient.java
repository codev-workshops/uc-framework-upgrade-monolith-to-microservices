package io.spring.application.clients;

import io.spring.application.data.AuthorRef;
import java.util.Optional;

/** Resolves user/author projections owned by user-auth-service. */
public interface UserAuthClient {
  /**
   * Resolve an author projection by user id via user-auth-service {@code GET /internal/users/{id}}.
   * Returns empty when the user does not exist (404).
   */
  Optional<AuthorRef> findById(String id, String authorization);
}
