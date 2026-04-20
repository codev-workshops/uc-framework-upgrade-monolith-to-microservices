package io.spring.commentservice.application;

import io.spring.commentservice.application.data.ProfileData;
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

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(userServiceUrl + "/internal/users/{id}", Map.class, userId);
      if (response.getBody() != null) {
        Map body = response.getBody();
        return new ProfileData(
            (String) body.get("id"),
            (String) body.get("username"),
            (String) body.get("bio"),
            (String) body.get("image"),
            false);
      }
      return null;
    } catch (Exception e) {
      return null;
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
}
