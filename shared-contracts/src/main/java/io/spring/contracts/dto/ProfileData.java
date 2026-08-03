package io.spring.contracts.dto;

import java.util.Objects;

/**
 * Author/profile projection returned to clients. {@code following} is resolved per current user by
 * profile-service; the remaining fields come from user-service.
 */
public class ProfileData {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;

  public ProfileData() {}

  public ProfileData(String id, String username, String bio, String image, boolean following) {
    this.id = id;
    this.username = username;
    this.bio = bio;
    this.image = image;
    this.following = following;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public boolean isFollowing() {
    return following;
  }

  public void setFollowing(boolean following) {
    this.following = following;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProfileData that = (ProfileData) o;
    return following == that.following
        && Objects.equals(id, that.id)
        && Objects.equals(username, that.username)
        && Objects.equals(bio, that.bio)
        && Objects.equals(image, that.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, bio, image, following);
  }
}
