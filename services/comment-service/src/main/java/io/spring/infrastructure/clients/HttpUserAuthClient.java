package io.spring.infrastructure.clients;

import io.spring.application.clients.UserAuthClient;
import io.spring.application.data.AuthorRef;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("!mock")
public class HttpUserAuthClient implements UserAuthClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpUserAuthClient(
      RestTemplate restTemplate, @Value("${services.user-auth.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<AuthorRef> findById(String id, String authorization) {
    try {
      HttpEntity<Void> request = new HttpEntity<>(ClientHeaders.forwarding(authorization));
      ResponseEntity<AuthorRef> response =
          restTemplate.exchange(
              baseUrl + "/internal/users/{id}", HttpMethod.GET, request, AuthorRef.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
