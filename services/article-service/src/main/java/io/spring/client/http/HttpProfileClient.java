package io.spring.client.http;

import io.spring.client.ProfileClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("!mock")
public class HttpProfileClient implements ProfileClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpProfileClient(
      RestTemplate upstreamRestTemplate, @Value("${services.profile.url}") String baseUrl) {
    this.restTemplate = upstreamRestTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public List<String> followedAuthors(String userId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/internal/follows/followed")
            .queryParam("userId", userId)
            .toUriString();
    String[] body = restTemplate.getForObject(url, String[].class);
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }

  @Override
  public List<String> followingAmong(String userId, List<String> candidateAuthorIds) {
    if (candidateAuthorIds.isEmpty()) {
      return Collections.emptyList();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/internal/follows/among")
            .queryParam("userId", userId)
            .toUriString();
    HttpEntity<List<String>> entity =
        new HttpEntity<>(candidateAuthorIds, ClientConfig.jsonHeaders());
    String[] body = restTemplate.exchange(url, HttpMethod.POST, entity, String[].class).getBody();
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }
}
