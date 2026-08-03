package io.spring.contracts.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Optional;

/**
 * Default HMAC-SHA verifier shared by all services. Constructed with the same signing secret that
 * user-service uses to issue tokens (property {@code jwt.secret}).
 */
public class DefaultJwtVerifier implements JwtVerifier {
  private final Key signingKey;

  public DefaultJwtVerifier(String secret) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      String subject =
          Jwts.parserBuilder()
              .setSigningKey(signingKey)
              .build()
              .parseClaimsJws(token)
              .getBody()
              .getSubject();
      return Optional.ofNullable(subject);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
