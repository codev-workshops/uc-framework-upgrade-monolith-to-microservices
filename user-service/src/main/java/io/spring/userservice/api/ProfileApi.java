package io.spring.userservice.api;

import io.spring.common.dto.ProfileData;
import io.spring.userservice.api.exception.ResourceNotFoundException;
import io.spring.userservice.application.ProfileQueryService;
import io.spring.userservice.core.FollowRelation;
import io.spring.userservice.core.UserRepository;
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
      @PathVariable("username") String username, @AuthenticationPrincipal String userId) {
    return profileQueryService
        .findByUsername(username, userId)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal String userId) {
    return userRepository
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(userId, target.getId());
              userRepository.saveRelation(followRelation);
              return profileResponse(profileQueryService.findByUsername(username, userId).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal String userId) {
    return userRepository
        .findByUsername(username)
        .map(
            target ->
                userRepository
                    .findRelation(userId, target.getId())
                    .map(
                        relation -> {
                          userRepository.removeRelation(relation);
                          return profileResponse(
                              profileQueryService.findByUsername(username, userId).get());
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
