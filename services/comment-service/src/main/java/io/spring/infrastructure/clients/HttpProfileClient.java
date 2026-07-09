package io.spring.infrastructure.clients;

import io.spring.application.clients.ProfileClient;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("!mock")
public class HttpProfileClient implements ProfileClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpProfileClient(
      RestTemplate restTemplate, @Value("${services.profile.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Set<String> followingAmong(
      String userId, List<String> candidateIds, String authorization) {
    if (candidateIds == null || candidateIds.isEmpty()) {
      return Collections.emptySet();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/follows/among")
            .queryParam("userId", userId)
            .toUriString();
    HttpEntity<List<String>> request =
        new HttpEntity<>(candidateIds, ClientHeaders.forwarding(authorization));
    ResponseEntity<List<String>> response =
        restTemplate.exchange(
            url, HttpMethod.POST, request, new ParameterizedTypeReference<List<String>>() {});
    List<String> body = response.getBody();
    return body == null ? Collections.emptySet() : new HashSet<>(body);
  }
}
