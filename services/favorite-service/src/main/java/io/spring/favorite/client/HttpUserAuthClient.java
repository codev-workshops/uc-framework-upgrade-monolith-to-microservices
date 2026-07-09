package io.spring.favorite.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUserAuthClient implements UserAuthClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpUserAuthClient(
      RestTemplate restTemplate, @Value("${userauth.service.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<String> findUserIdByUsername(String username) {
    try {
      AuthorRef ref =
          restTemplate.getForObject(
              baseUrl + "/internal/users/by-username/{username}", AuthorRef.class, username);
      return Optional.ofNullable(ref).map(AuthorRef::getId);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
