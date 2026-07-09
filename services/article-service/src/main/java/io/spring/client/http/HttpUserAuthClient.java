package io.spring.client.http;

import io.spring.client.UserAuthClient;
import io.spring.client.dto.AuthorRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("!mock")
public class HttpUserAuthClient implements UserAuthClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpUserAuthClient(
      RestTemplate upstreamRestTemplate, @Value("${services.user-auth.url}") String baseUrl) {
    this.restTemplate = upstreamRestTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<AuthorRef> findById(String id) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(baseUrl)
              .path("/internal/users/{id}")
              .buildAndExpand(id)
              .toUriString();
      return Optional.ofNullable(restTemplate.getForObject(url, AuthorRef.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<AuthorRef> findByUsername(String username) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(baseUrl)
              .path("/internal/users/by-username/{username}")
              .buildAndExpand(username)
              .toUriString();
      return Optional.ofNullable(restTemplate.getForObject(url, AuthorRef.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public List<AuthorRef> findByIds(List<String> ids) {
    if (ids.isEmpty()) {
      return Collections.emptyList();
    }
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl).path("/internal/users/batch").toUriString();
    HttpEntity<List<String>> entity = new HttpEntity<>(ids, ClientConfig.jsonHeaders());
    AuthorRef[] body =
        restTemplate.exchange(url, HttpMethod.POST, entity, AuthorRef[].class).getBody();
    return body == null ? Collections.emptyList() : Arrays.asList(body);
  }
}
