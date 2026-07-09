package io.spring.profileservice.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical author projection returned by user-auth-service (see contracts/dto/shared-dtos.md).
 * `id` is used only internally to correlate follow flags.
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
