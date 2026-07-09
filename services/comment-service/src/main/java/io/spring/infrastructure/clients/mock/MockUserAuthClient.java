package io.spring.infrastructure.clients.mock;

import io.spring.application.clients.UserAuthClient;
import io.spring.application.data.AuthorRef;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stand-in for user-auth-service used under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockUserAuthClient implements UserAuthClient {
  @Override
  public Optional<AuthorRef> findById(String id, String authorization) {
    return Optional.of(
        new AuthorRef(
            id, id, "mock bio", "https://static.productionready.io/images/smiley-cyrus.jpg"));
  }
}
