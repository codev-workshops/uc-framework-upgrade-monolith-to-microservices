package io.spring.userservice.api;

import io.spring.shared.auth.UserPrincipal;
import io.spring.userservice.api.exception.ResourceNotFoundException;
import io.spring.userservice.application.ProfileQueryService;
import io.spring.userservice.application.data.ProfileData;
import io.spring.userservice.core.user.FollowRelation;
import io.spring.userservice.core.user.UserRepository;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal UserPrincipal user) {
    String userId = user != null ? user.getId() : null;
    return profileQueryService
        .findByUsername(username, userId)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal UserPrincipal user) {
    return userRepository
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              userRepository.saveRelation(followRelation);
              return profileResponse(
                  profileQueryService.findByUsername(username, user.getId()).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal UserPrincipal user) {
    return userRepository
        .findByUsername(username)
        .map(
            target ->
                userRepository
                    .findRelation(user.getId(), target.getId())
                    .map(
                        relation -> {
                          userRepository.removeRelation(relation);
                          return profileResponse(
                              profileQueryService.findByUsername(username, user.getId()).get());
                        })
                    .orElseThrow(ResourceNotFoundException::new))
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
