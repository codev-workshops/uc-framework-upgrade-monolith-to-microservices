package io.spring.article.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8082}") String url) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = url;
  }

  public Set<String> getFollowingAuthors(String userId, List<String> targetIds) {
    if (targetIds == null || targetIds.isEmpty()) {
      return Collections.emptySet();
    }
    String ids = String.join(",", targetIds);
    String url =
        userServiceUrl
            + "/internal/users/"
            + userId
            + "/following?targetIds="
            + ids;
    try {
      String[] result = restTemplate.getForObject(url, String[].class);
      if (result != null) {
        return new HashSet<>(Arrays.asList(result));
      }
    } catch (Exception e) {
      // fallback to empty
    }
    return Collections.emptySet();
  }

  public List<String> getFollowedUsers(String userId) {
    String url = userServiceUrl + "/internal/users/" + userId + "/followed-users";
    try {
      String[] result = restTemplate.getForObject(url, String[].class);
      if (result != null) {
        return Arrays.stream(result).collect(Collectors.toList());
      }
    } catch (Exception e) {
      // fallback to empty
    }
    return Collections.emptyList();
  }
}
