package io.spring.profileservice.infrastructure.client;

import java.util.Optional;

/**
 * Client for the upstream user-auth-service (profile-service does not own `users`). Resolves user
 * identity via the frozen user-auth contract:
 *
 * <ul>
 *   <li>{@code GET /internal/users/by-username/{username}}
 *   <li>{@code GET /internal/users/{id}}
 * </ul>
 */
public interface UserAuthClient {
  Optional<AuthorRef> findByUsername(String username);

  Optional<AuthorRef> findById(String id);
}
