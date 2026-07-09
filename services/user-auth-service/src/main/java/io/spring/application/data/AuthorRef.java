package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical author projection returned by the internal user-lookup endpoints. Non-user services
 * embed this instead of joining the {@code users} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRef {
  private String id;
  private String username;
  private String bio;
  private String image;

  public static AuthorRef fromUserData(UserData userData) {
    return new AuthorRef(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage());
  }
}
