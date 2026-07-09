package io.spring.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical author projection returned by user-auth-service. Embedded by article-service instead of
 * joining the {@code users} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRef {
  private String id;
  private String username;
  private String bio;
  private String image;
}
