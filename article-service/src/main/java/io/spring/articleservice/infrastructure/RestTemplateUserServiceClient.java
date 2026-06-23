package io.spring.articleservice.infrastructure;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.ProfileData;
import io.spring.shared.dto.UserData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateUserServiceClient implements UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public RestTemplateUserServiceClient(
      RestTemplate restTemplate,
      @Value("${services.user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public Optional<UserData> getUserById(String id) {
    try {
      String url = userServiceUrl + "/internal/users/" + id;
      ResponseEntity<UserData> response = restTemplate.getForEntity(url, UserData.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  @Override
  public Map<String, Boolean> getFollowingAuthors(String userId, List<String> targetIds) {
    try {
      String ids = String.join(",", targetIds);
      String url =
          userServiceUrl
              + "/internal/users/"
              + userId
              + "/following?targetIds="
              + ids;
      ResponseEntity<Map<String, Boolean>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Boolean>>() {});
      return response.getBody() != null ? response.getBody() : Collections.emptyMap();
    } catch (RestClientException e) {
      return Collections.emptyMap();
    }
  }

  @Override
  public boolean isFollowing(String userId, String targetId) {
    try {
      String url =
          userServiceUrl
              + "/internal/users/"
              + userId
              + "/following/"
              + targetId;
      ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
      return response.getBody() != null && response.getBody();
    } catch (RestClientException e) {
      return false;
    }
  }

  @Override
  public Optional<ProfileData> getProfile(String userId, String currentUserId) {
    try {
      String url =
          userServiceUrl
              + "/internal/users/"
              + userId
              + "/profile?currentUserId="
              + currentUserId;
      ResponseEntity<ProfileData> response =
          restTemplate.getForEntity(url, ProfileData.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }
}
