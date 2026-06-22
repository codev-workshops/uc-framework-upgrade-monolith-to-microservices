package io.spring.shared.auth;

import java.util.Optional;

public interface JwtService {
    Optional<String> getSubFromToken(String token);
}
