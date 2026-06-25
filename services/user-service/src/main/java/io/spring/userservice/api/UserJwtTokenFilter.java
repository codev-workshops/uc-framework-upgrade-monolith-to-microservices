package io.spring.userservice.api;

import io.spring.shared.security.JwtService;
import io.spring.shared.security.JwtTokenFilter;
import io.spring.userservice.domain.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserJwtTokenFilter extends JwtTokenFilter {

  private final UserRepository userRepository;

  public UserJwtTokenFilter(JwtService jwtService, UserRepository userRepository) {
    super(jwtService);
    this.userRepository = userRepository;
  }

  @Override
  protected Object loadUser(String userId) {
    return userRepository.findById(userId).orElse(null);
  }
}
