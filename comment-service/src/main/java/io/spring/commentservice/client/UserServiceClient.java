package io.spring.commentservice.client;

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
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public boolean isUserFollowing(String userId, String targetId) {
    String url =
        userServiceUrl + "/internal/users/" + userId + "/following?targetId=" + targetId;
    Boolean result = restTemplate.getForObject(url, Boolean.class);
    return result != null && result;
  }

  public Set<String> getFollowingAuthors(String userId, List<String> authorIds) {
    String ids = authorIds.stream().collect(Collectors.joining(","));
    String url =
        userServiceUrl
            + "/internal/users/following-authors?userId="
            + userId
            + "&authorIds="
            + ids;
    ResponseEntity<Set<String>> response =
        restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {});
    Set<String> body = response.getBody();
    return body != null ? body : new HashSet<>();
  }
}
