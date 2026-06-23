package io.spring.articleservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.articleservice.api.security.AuthenticatedUser;
import io.spring.articleservice.application.data.UserData;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.security.JwtUtils;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {
  @MockBean protected UserServiceClient userServiceClient;

  protected AuthenticatedUser user;
  protected UserData userData;
  protected String token;
  protected String email;
  protected String username;
  protected String defaultAvatar;

  @MockBean protected JwtUtils jwtUtils;

  protected void userFixture() {
    email = "john@jacob.com";
    username = "johnjacob";
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new AuthenticatedUser("user-id-123", username, email, "", defaultAvatar);
    io.spring.shared.dto.UserData sharedUserData =
        new io.spring.shared.dto.UserData(user.getId(), email, username, "", defaultAvatar);
    when(userServiceClient.getUserById(eq(user.getId()))).thenReturn(Optional.of(sharedUserData));

    userData = new UserData(user.getId(), email, username, "", defaultAvatar);

    token = "token";
    when(jwtUtils.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
