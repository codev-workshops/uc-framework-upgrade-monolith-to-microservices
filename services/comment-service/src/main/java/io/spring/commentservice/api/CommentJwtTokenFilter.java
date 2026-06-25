package io.spring.commentservice.api;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.security.JwtService;
import io.spring.shared.security.JwtTokenFilter;
import org.springframework.stereotype.Component;

@Component
public class CommentJwtTokenFilter extends JwtTokenFilter {

  private final UserServiceClient userServiceClient;

  public CommentJwtTokenFilter(JwtService jwtService, UserServiceClient userServiceClient) {
    super(jwtService);
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected Object loadUser(String userId) {
    return userServiceClient.findUserById(userId).orElse(null);
  }
}
