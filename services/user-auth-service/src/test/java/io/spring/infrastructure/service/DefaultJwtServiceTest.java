package io.spring.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class DefaultJwtServiceTest {

  private static final String SECRET =
      "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA";

  @Test
  public void should_issue_and_validate_token_with_user_id_as_subject() {
    JwtService jwtService = new DefaultJwtService(SECRET, 3600);
    User user = new User("e@x.com", "jake", "pass", "", "");

    String token = jwtService.toToken(user);

    Optional<String> sub = jwtService.getSubFromToken(token);
    assertTrue(sub.isPresent());
    assertEquals(user.getId(), sub.get());
  }

  @Test
  public void should_return_empty_for_garbage_token() {
    JwtService jwtService = new DefaultJwtService(SECRET, 3600);
    assertFalse(jwtService.getSubFromToken("not-a-jwt").isPresent());
  }

  @Test
  public void should_return_empty_for_expired_token() {
    JwtService jwtService = new DefaultJwtService(SECRET, -10);
    User user = new User("e@x.com", "jake", "pass", "", "");
    String token = jwtService.toToken(user);
    assertFalse(jwtService.getSubFromToken(token).isPresent());
  }

  @Test
  public void should_reject_token_signed_with_different_key() {
    JwtService issuer = new DefaultJwtService(SECRET, 3600);
    JwtService verifier =
        new DefaultJwtService("a-completely-different-secret-key-that-is-long-enough-xxxx", 3600);
    User user = new User("e@x.com", "jake", "pass", "", "");
    String token = issuer.toToken(user);
    assertFalse(verifier.getSubFromToken(token).isPresent());
  }
}
