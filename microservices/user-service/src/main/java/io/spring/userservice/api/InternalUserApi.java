package io.spring.userservice.api;

import io.spring.userservice.application.ProfileQueryService;
import io.spring.userservice.application.UserQueryService;
import io.spring.userservice.application.data.ProfileData;
import io.spring.userservice.application.data.UserData;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserQueryService userQueryService;
  private ProfileQueryService profileQueryService;

  @GetMapping("/{id}")
  public ResponseEntity getUserById(@PathVariable("id") String id) {
    Optional<UserData> userData = userQueryService.findById(id);
    return userData.map(data -> ResponseEntity.ok(data)).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/profile/{username}")
  public ResponseEntity getProfileByUsername(@PathVariable("username") String username) {
    Optional<ProfileData> profileData = profileQueryService.findByUsername(username, null);
    return profileData
        .map(data -> ResponseEntity.ok(data))
        .orElse(ResponseEntity.notFound().build());
  }
}
