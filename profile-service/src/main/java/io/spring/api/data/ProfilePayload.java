package io.spring.api.data;

import io.spring.contracts.dto.ProfileData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Public RealWorld profile shape: the internal id is not exposed to clients. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePayload {
  private String username;
  private String bio;
  private String image;
  private boolean following;

  public static ProfilePayload from(ProfileData profile) {
    return new ProfilePayload(
        profile.getUsername(), profile.getBio(), profile.getImage(), profile.isFollowing());
  }
}
