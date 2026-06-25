package io.spring.favoriteservice.api;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.security.JwtService;
import io.spring.shared.security.JwtTokenFilter;
import org.springframework.stereotype.Component;

@Component
public class FavoriteJwtTokenFilter extends JwtTokenFilter {

  private final UserServiceClient userServiceClient;

  public FavoriteJwtTokenFilter(JwtService jwtService, UserServiceClient userServiceClient) {
    super(jwtService);
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected Object loadUser(String userId) {
    return userServiceClient.findUserById(userId).orElse(null);
  }
}
