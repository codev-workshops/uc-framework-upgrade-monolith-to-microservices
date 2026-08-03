package io.spring.infrastructure.client;

import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.dto.ProfileData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Resolves the {@code following} flag for comment authors from profile-service over HTTP. */
@Component
public class RestProfileServiceClient implements ProfileServiceClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestProfileServiceClient(
      RestTemplate restTemplate, @Value("${services.profile.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<ProfileData> findProfile(String username, String currentUserId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/profiles/" + username)
            .queryParamIfPresent("currentUserId", Optional.ofNullable(currentUserId))
            .toUriString();
    try {
      return Optional.ofNullable(restTemplate.getForObject(url, ProfileData.class));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean isFollowing(String currentUserId, String targetUserId) {
    if (currentUserId == null || targetUserId == null) {
      return false;
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/follows/is-following")
            .queryParam("currentUserId", currentUserId)
            .queryParam("targetUserId", targetUserId)
            .toUriString();
    try {
      return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public Set<String> followingAuthors(String currentUserId, List<String> authorIds) {
    if (currentUserId == null || authorIds == null || authorIds.isEmpty()) {
      return Set.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/follows/following-authors")
            .queryParam("currentUserId", currentUserId)
            .queryParam("authorIds", String.join(",", authorIds))
            .toUriString();
    try {
      Set<String> following =
          restTemplate
              .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {})
              .getBody();
      return following == null ? Set.of() : following;
    } catch (RuntimeException e) {
      return Set.of();
    }
  }

  @Override
  public List<String> followedUsers(String userId) {
    if (userId == null) {
      return List.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/follows/followed-users")
            .queryParam("userId", userId)
            .toUriString();
    try {
      List<String> users =
          restTemplate
              .exchange(
                  url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {})
              .getBody();
      return users == null ? List.of() : users;
    } catch (RuntimeException e) {
      return List.of();
    }
  }
}
