package io.spring.favorite.client;

import java.util.Optional;

/**
 * Resolves a username to a user id via user-auth-service. favorite-service stores ids only; this
 * is used solely by {@code GET /internal/favorites/by-user} when a caller passes a username
 * instead of a userId.
 */
public interface UserAuthClient {
  Optional<String> findUserIdByUsername(String username);
}
