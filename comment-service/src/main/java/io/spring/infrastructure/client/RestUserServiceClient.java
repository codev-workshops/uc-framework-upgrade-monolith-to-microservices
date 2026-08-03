package io.spring.infrastructure.client;

import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.UserSummary;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Resolves comment authors (username/bio/image) from user-service over HTTP. */
@Component
public class RestUserServiceClient implements UserServiceClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestUserServiceClient(
      RestTemplate restTemplate, @Value("${services.user.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<UserSummary> findById(String id) {
    return get(baseUrl + "/internal/users/" + id);
  }

  @Override
  public Optional<UserSummary> findByUsername(String username) {
    return get(baseUrl + "/internal/users/by-username/" + username);
  }

  @Override
  public List<UserSummary> findByIds(Collection<String> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/users")
            .queryParam("ids", String.join(",", ids))
            .toUriString();
    try {
      List<UserSummary> summaries =
          restTemplate
              .exchange(
                  url, HttpMethod.GET, null, new ParameterizedTypeReference<List<UserSummary>>() {})
              .getBody();
      return summaries == null ? List.of() : summaries;
    } catch (RuntimeException e) {
      return List.of();
    }
  }

  private Optional<UserSummary> get(String url) {
    try {
      return Optional.ofNullable(restTemplate.getForObject(url, UserSummary.class));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }
}
