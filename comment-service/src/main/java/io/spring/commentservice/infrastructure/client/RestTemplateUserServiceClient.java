package io.spring.commentservice.infrastructure.client;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.ProfileData;
import io.spring.shared.dto.UserData;
import io.spring.shared.exception.ServiceUnavailableException;
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
      ResponseEntity<UserData> response =
          restTemplate.getForEntity(userServiceUrl + "/internal/users/{id}", UserData.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("User Service", e);
    }
  }

  @Override
  public Map<String, Boolean> getFollowingAuthors(String userId, List<String> targetIds) {
    try {
      Map<String, Object> request = new HashMap<>();
      request.put("userId", userId);
      request.put("targetIds", targetIds);
      ResponseEntity<Map<String, Boolean>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/following",
              HttpMethod.POST,
              new org.springframework.http.HttpEntity<>(request),
              new ParameterizedTypeReference<Map<String, Boolean>>() {});
      Map<String, Boolean> body = response.getBody();
      return body != null ? body : Collections.emptyMap();
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("User Service", e);
    }
  }

  @Override
  public boolean isFollowing(String userId, String targetId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/following/{targetId}",
              Boolean.class,
              userId,
              targetId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("User Service", e);
    }
  }

  @Override
  public Optional<ProfileData> getProfile(String userId, String currentUserId) {
    try {
      ResponseEntity<ProfileData> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/profile?currentUserId={currentUserId}",
              ProfileData.class,
              userId,
              currentUserId);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("User Service", e);
    }
  }
}
