package io.spring.contracts.security;

import java.util.Optional;

/**
 * Shared JWT verification contract. user-service issues tokens; every other service verifies the
 * bearer token and extracts the subject (user id) with this contract, so no service other than
 * user-service needs access to the {@code users} table to authenticate a request.
 */
public interface JwtVerifier {
  /**
   * Verify the token signature/expiry and return the subject (user id) if valid.
   *
   * @param token the raw JWT (without the {@code Bearer } prefix)
   * @return the user id encoded as the token subject, or empty if the token is invalid/expired
   */
  Optional<String> getSubFromToken(String token);
}
