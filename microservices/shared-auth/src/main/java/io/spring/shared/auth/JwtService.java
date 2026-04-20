package io.spring.shared.auth;

import java.util.Optional;

public interface JwtService {
  String toToken(UserPrincipal user);

  Optional<String> getSubFromToken(String token);
}
