package io.spring.favoriteservice.client;

import io.spring.common.data.UserData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
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
    return restTemplate.getForObject(
        userServiceUrl + "/internal/users/{id}", UserData.class, userId);
  }

  public boolean isUserFollowing(String userId, String targetId) {
    Boolean result =
        restTemplate.getForObject(
            userServiceUrl + "/internal/users/{userId}/following/{targetId}",
            Boolean.class,
            userId,
            targetId);
    return result != null && result;
  }

  public Set<String> getFollowingAuthors(String userId, List<String> authorIds) {
    String ids = String.join(",", authorIds);
    String[] result =
        restTemplate.getForObject(
            userServiceUrl + "/internal/users/{userId}/following?ids={ids}",
            String[].class,
            userId,
            ids);
    return result != null ? new HashSet<>(Arrays.asList(result)) : new HashSet<>();
  }
}
