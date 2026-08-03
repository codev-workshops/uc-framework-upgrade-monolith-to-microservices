package io.spring.contracts.client;

import io.spring.contracts.dto.UserSummary;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Contract other services use to resolve base user data from user-service. */
public interface UserServiceClient {
  Optional<UserSummary> findById(String id);

  Optional<UserSummary> findByUsername(String username);

  List<UserSummary> findByIds(Collection<String> ids);
}
