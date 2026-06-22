package io.spring.articleservice.client;

import io.spring.common.data.UserData;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public UserData getUserById(String userId) {
    try {
      return restTemplate.getForObject(
          userServiceUrl + "/internal/users/{id}", UserData.class, userId);
    } catch (Exception e) {
      return null;
    }
  }

  public UserData getUserByUsername(String username) {
    try {
      return restTemplate.getForObject(
          userServiceUrl + "/internal/users/by-username/{username}", UserData.class, username);
    } catch (Exception e) {
      return null;
    }
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/{userId}/following?targetId={targetId}",
              Boolean.class,
              userId,
              targetId);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public List<String> getFollowedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/followed-users",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      return response.getBody() != null ? response.getBody() : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public Set<String> getFollowingAuthors(String userId, List<String> authorIds) {
    try {
      String authorIdsParam = String.join(",", authorIds);
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              userServiceUrl
                  + "/internal/users/following-authors?userId={userId}&authorIds={authorIds}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Set<String>>() {},
              userId,
              authorIdsParam);
      return response.getBody() != null ? response.getBody() : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }
}
