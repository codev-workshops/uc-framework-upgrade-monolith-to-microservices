package io.spring.favoriteservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.shared.security.JwtUtils;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {

  @MockBean protected JwtUtils jwtUtils;

  protected String userId;
  protected String token;

  protected void userFixture() {
    userId = "user-id-123";
    token = "token";
    when(jwtUtils.getSubFromToken(eq(token))).thenReturn(Optional.of(userId));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
