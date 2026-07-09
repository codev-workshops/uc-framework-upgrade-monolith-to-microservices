package io.spring.profileservice.security;

import java.util.Optional;

/**
 * Validate-only JWT service. Unlike the monolith's JwtService, profile-service never issues tokens
 * (only user-auth-service does) — it only extracts the subject (user id) from a presented token.
 */
public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
