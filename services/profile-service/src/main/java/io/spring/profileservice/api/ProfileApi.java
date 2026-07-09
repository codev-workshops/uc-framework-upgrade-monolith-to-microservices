package io.spring.profileservice.api;

import io.spring.profileservice.api.exception.ResourceNotFoundException;
import io.spring.profileservice.application.ProfileQueryService;
import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.core.follow.FollowRelation;
import io.spring.profileservice.core.follow.FollowRepository;
import io.spring.profileservice.infrastructure.client.AuthorRef;
import io.spring.profileservice.infrastructure.client.UserAuthClient;
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
  private final ProfileQueryService profileQueryService;
  private final FollowRepository followRepository;
  private final UserAuthClient userAuthClient;

  @GetMapping
  public ResponseEntity<?> getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    return profileQueryService
        .findByUsername(username, normalize(currentUserId))
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private static String normalize(String principal) {
    return "anonymousUser".equals(principal) ? null : principal;
  }

  @PostMapping(path = "follow")
  public ResponseEntity<?> follow(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    AuthorRef target =
        userAuthClient.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    followRepository.saveRelation(new FollowRelation(currentUserId, target.getId()));
    return profileResponse(profileQueryService.toProfileData(target, currentUserId));
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity<?> unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    AuthorRef target =
        userAuthClient.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    followRepository
        .findRelation(currentUserId, target.getId())
        .ifPresent(followRepository::removeRelation);
    return profileResponse(profileQueryService.toProfileData(target, currentUserId));
  }

  private ResponseEntity<?> profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
