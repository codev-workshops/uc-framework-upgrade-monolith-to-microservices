package io.spring.profileservice.infrastructure.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP implementation of {@link UserAuthClient}. Forwards the incoming {@code Authorization} header
 * on outbound calls so downstream services resolve the same current user (see
 * contracts/jwt-contract.md). Active unless the {@code mock} profile is selected.
 */
@Component
@Profile("!mock")
public class HttpUserAuthClient implements UserAuthClient {

  private final RestTemplate restTemplate;
  private final String userAuthUrl;

  public HttpUserAuthClient(
      RestTemplate restTemplate, @Value("${services.user-auth.url}") String userAuthUrl) {
    this.restTemplate = restTemplate;
    this.userAuthUrl = userAuthUrl;
  }

  @Override
  public Optional<AuthorRef> findByUsername(String username) {
    return get("/internal/users/by-username/{key}", username);
  }

  @Override
  public Optional<AuthorRef> findById(String id) {
    return get("/internal/users/{key}", id);
  }

  private Optional<AuthorRef> get(String path, String key) {
    try {
      ResponseEntity<AuthorRef> response =
          restTemplate.exchange(
              userAuthUrl + path,
              HttpMethod.GET,
              new HttpEntity<>(forwardAuthHeader()),
              AuthorRef.class,
              key);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  private HttpHeaders forwardAuthHeader() {
    HttpHeaders headers = new HttpHeaders();
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      String auth = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
      if (auth != null) {
        headers.set(HttpHeaders.AUTHORIZATION, auth);
      }
    }
    return headers;
  }
}
