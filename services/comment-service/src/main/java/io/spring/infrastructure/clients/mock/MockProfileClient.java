package io.spring.infrastructure.clients.mock;

import io.spring.application.clients.ProfileClient;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stand-in for profile-service used under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockProfileClient implements ProfileClient {
  @Override
  public Set<String> followingAmong(
      String userId, List<String> candidateIds, String authorization) {
    return Collections.emptySet();
  }
}
