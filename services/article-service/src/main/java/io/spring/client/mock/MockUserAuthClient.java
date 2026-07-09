package io.spring.client.mock;

import io.spring.client.UserAuthClient;
import io.spring.client.dto.AuthorRef;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory stub used when running standalone under the {@code mock} profile. */
@Component
@Profile("mock")
public class MockUserAuthClient implements UserAuthClient {
  private final Map<String, AuthorRef> byId = new LinkedHashMap<>();

  public MockUserAuthClient() {
    add(new AuthorRef("user-1", "jake", "I work at state farm", ""));
    add(new AuthorRef("user-2", "john", "", ""));
    add(new AuthorRef("user-3", "jane", "", ""));
  }

  private void add(AuthorRef ref) {
    byId.put(ref.getId(), ref);
  }

  @Override
  public Optional<AuthorRef> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public Optional<AuthorRef> findByUsername(String username) {
    return byId.values().stream().filter(a -> a.getUsername().equals(username)).findFirst();
  }

  @Override
  public List<AuthorRef> findByIds(List<String> ids) {
    return ids.stream()
        .map(byId::get)
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());
  }
}
