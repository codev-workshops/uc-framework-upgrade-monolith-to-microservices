package io.spring.client;

import io.spring.client.dto.AuthorRef;
import java.util.List;
import java.util.Optional;

/** Read-only view of user-auth-service used for author projection and username resolution. */
public interface UserAuthClient {

  /** {@code GET /internal/users/{id}} -> author projection. */
  Optional<AuthorRef> findById(String id);

  /** {@code GET /internal/users/by-username/{username}} -> author projection. */
  Optional<AuthorRef> findByUsername(String username);

  /** {@code POST /internal/users/batch} -> author projections for the given ids. */
  List<AuthorRef> findByIds(List<String> ids);
}
