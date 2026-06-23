package io.spring.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Shared JWT validation utility. All microservices use this to validate tokens issued by the User
 * Service.
 */
public class JwtUtils {

  private final SecretKey signingKey;

  public JwtUtils(String secret) {
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    this.signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
  }

  /** Extract user ID (subject) from a JWT token. */
  public Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /** Extract bearer token from Authorization header value. */
  public static Optional<String> extractTokenFromHeader(String authorizationHeader) {
    if (authorizationHeader == null) {
      return Optional.empty();
    }
    String[] split = authorizationHeader.split(" ");
    if (split.length < 2) {
      return Optional.empty();
    }
    return Optional.ofNullable(split[1]);
  }
}
