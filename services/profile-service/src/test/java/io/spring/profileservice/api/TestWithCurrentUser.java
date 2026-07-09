package io.spring.profileservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.profileservice.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {

  @MockBean protected JwtService jwtService;

  protected String currentUserId;
  protected String token;

  protected void userFixture() {
    currentUserId = "user-1";
    token = "valid-token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(currentUserId));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
