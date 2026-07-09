package io.spring.profileservice.infrastructure.client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * In-memory stub of {@link UserAuthClient}, active under the {@code mock} Spring profile. Lets
 * profile-service run and be demoed without a live user-auth-service. Seeded with the same user ids
 * used in the follows seed data (contracts/migrations/profile-service/V2__seed_data.sql).
 */
@Component
@Profile("mock")
public class MockUserAuthClient implements UserAuthClient {

  private final ConcurrentMap<String, AuthorRef> byId = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, AuthorRef> byUsername = new ConcurrentHashMap<>();

  @PostConstruct
  void seed() {
    add(new AuthorRef("user-1", "jake", "I work at state farm", ""));
    add(new AuthorRef("user-2", "john", "", ""));
    add(new AuthorRef("user-3", "jane", "", ""));
  }

  private void add(AuthorRef ref) {
    byId.put(ref.getId(), ref);
    byUsername.put(ref.getUsername(), ref);
  }

  @Override
  public Optional<AuthorRef> findByUsername(String username) {
    return Optional.ofNullable(byUsername.get(username));
  }

  @Override
  public Optional<AuthorRef> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }
}
