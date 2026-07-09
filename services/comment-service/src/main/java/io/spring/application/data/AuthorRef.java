package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical author projection returned by user-auth-service {@code GET /internal/users/{id}}.
 * Replaces the removed {@code comments}->{@code users} join.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorRef {
  private String id;
  private String username;
  private String bio;
  private String image;
}
