package io.spring.shared.auth;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
  String toToken(UserPrincipal user);

  Optional<String> getSubFromToken(String token);

  Optional<UserPrincipal> getUserFromToken(String token);
}
