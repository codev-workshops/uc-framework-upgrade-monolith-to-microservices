package io.spring.client.mock;

import io.spring.client.ProfileClient;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stub used when running standalone under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockProfileClient implements ProfileClient {

  @Override
  public List<String> followedAuthors(String userId) {
    return Collections.emptyList();
  }

  @Override
  public List<String> followingAmong(String userId, List<String> candidateAuthorIds) {
    return Collections.emptyList();
  }
}
