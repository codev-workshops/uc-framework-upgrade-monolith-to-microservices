package io.spring.commentservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.UserData;
import io.spring.shared.security.JwtUtils;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {
  @MockBean protected UserServiceClient userServiceClient;

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

    userData = new UserData("user-id-1", email, username, "", defaultAvatar);
    when(userServiceClient.getUserById(eq(userData.getId()))).thenReturn(Optional.of(userData));

    token = "token";
    when(jwtUtils.getSubFromToken(eq(token))).thenReturn(Optional.of(userData.getId()));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
