package io.spring.favorite.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Canonical author projection returned by user-auth-service {@code GET /internal/users/...}. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorRef {
  private String id;
  private String username;
  private String bio;
  private String image;
}
