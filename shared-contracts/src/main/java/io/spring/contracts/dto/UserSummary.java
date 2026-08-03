package io.spring.contracts.dto;

import java.util.Objects;

/**
 * Cross-service projection of a user, resolved from user-service so that other services can render
 * author information without touching the {@code users} table.
 */
public class UserSummary {
  private String id;
  private String username;
  private String bio;
  private String image;

  public UserSummary() {}

  public UserSummary(String id, String username, String bio, String image) {
    this.id = id;
    this.username = username;
    this.bio = bio;
    this.image = image;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserSummary that = (UserSummary) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
