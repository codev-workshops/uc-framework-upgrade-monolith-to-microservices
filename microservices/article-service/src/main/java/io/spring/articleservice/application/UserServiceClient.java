package io.spring.articleservice.application;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/follows/{userId}/is-following/{targetId}",
              Boolean.class,
              userId,
              targetId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      Map<String, Object> request = new HashMap<>();
      request.put("userId", userId);
      request.put("authorIds", authorIds);
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/follows/following-authors",
              HttpMethod.POST,
              new HttpEntity<>(request),
              new ParameterizedTypeReference<Set<String>>() {});
      return response.getBody() != null ? response.getBody() : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }

  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/follows/{userId}/followed-users",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      return response.getBody() != null ? response.getBody() : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<UserData> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{id}", UserData.class, userId);
      if (response.getBody() != null) {
        UserData user = response.getBody();
        return new ProfileData(
            user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  private static class UserData {
    private String id;
    private String email;
    private String username;
    private String bio;
    private String image;
  }
}
