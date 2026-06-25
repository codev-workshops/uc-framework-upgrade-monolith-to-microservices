package io.spring.shared.client;

import io.spring.shared.data.ProfileData;
import io.spring.shared.data.UserData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${services.user-service.url:http://localhost:8081}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserData> findUserById(String userId) {
    try {
      UserData user =
          restTemplate.getForObject(userServiceUrl + "/internal/users/{id}", UserData.class, userId);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<UserData> findUserByUsername(String username) {
    try {
      UserData user =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/by-username/{username}",
              UserData.class,
              username);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public boolean isFollowing(String userId, String targetId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/{userId}/following/{targetId}",
              Boolean.class,
              userId,
              targetId);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    String url =
        UriComponentsBuilder.fromHttpUrl(userServiceUrl + "/internal/users/{userId}/following-among")
            .queryParam("ids", authorIds.toArray())
            .buildAndExpand(userId)
            .toUriString();
    Set<String> result =
        restTemplate
            .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {})
            .getBody();
    return result != null ? result : java.util.Collections.emptySet();
  }

  public List<String> followedUsers(String userId) {
    String url = userServiceUrl + "/internal/users/{userId}/followed";
    List<String> result =
        restTemplate
            .exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {},
                userId)
            .getBody();
    return result != null ? result : java.util.Collections.emptyList();
  }

  public ProfileData getProfile(String username, String currentUserId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(
                userServiceUrl + "/internal/profiles/{username}")
            .queryParam("currentUserId", currentUserId)
            .buildAndExpand(username)
            .toUriString();
    return restTemplate.getForObject(url, ProfileData.class);
  }
}
