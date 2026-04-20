package io.spring.articleservice.client;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<ProfileData> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/profile", ProfileData.class, userId);
      return response.getBody();
    } catch (Exception e) {
      return null;
    }
  }

  public boolean isFollowing(String userId, String targetUserId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/following/{targetUserId}",
              Boolean.class,
              userId,
              targetUserId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      String ids = String.join(",", authorIds);
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/following?ids={ids}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Set<String>>() {},
              userId,
              ids);
      Set<String> body = response.getBody();
      return body != null ? body : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }

  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/followed",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      List<String> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public String getUsernameById(String userId) {
    try {
      ProfileData profile = getProfile(userId);
      return profile != null ? profile.getUsername() : null;
    } catch (Exception e) {
      return null;
    }
  }

  public String getUserIdByUsername(String username) {
    try {
      ResponseEntity<String> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/username/{username}/id", String.class, username);
      return response.getBody();
    } catch (Exception e) {
      return null;
    }
  }
}
