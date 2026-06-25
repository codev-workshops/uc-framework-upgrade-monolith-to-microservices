package io.spring.articleservice.api;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.data.UserData;
import io.spring.shared.security.JwtService;
import io.spring.shared.security.JwtTokenFilter;
import org.springframework.stereotype.Component;

@Component
public class ArticleJwtTokenFilter extends JwtTokenFilter {

  private final UserServiceClient userServiceClient;

  public ArticleJwtTokenFilter(JwtService jwtService, UserServiceClient userServiceClient) {
    super(jwtService);
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected Object loadUser(String userId) {
    return userServiceClient.findUserById(userId).orElse(null);
  }
}
