package io.spring.commentservice.client;

import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.application.data.UserData;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserData> findById(String id) {
    try {
      UserData userData =
          restTemplate.getForObject(userServiceUrl + "/internal/users/{id}", UserData.class, id);
      return Optional.ofNullable(userData);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<ProfileData> findProfileByUsername(String username) {
    try {
      ProfileData profileData =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/profile/{username}", ProfileData.class, username);
      return Optional.ofNullable(profileData);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
