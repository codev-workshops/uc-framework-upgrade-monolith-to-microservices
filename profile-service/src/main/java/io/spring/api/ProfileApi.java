package io.spring.api;

import io.spring.api.data.ProfilePayload;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.FollowRepository;
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
  private final UserServiceClient userServiceClient;

  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    return profileQueryService
        .findByUsername(username, currentUserId)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    return userServiceClient
        .findByUsername(username)
        .map(
            target -> {
              followRepository.saveRelation(new FollowRelation(currentUserId, target.getId()));
              return profileResponse(
                  profileQueryService.findByUsername(username, currentUserId).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal String currentUserId) {
    return userServiceClient
        .findByUsername(username)
        .flatMap(target -> followRepository.findRelation(currentUserId, target.getId()))
        .map(
            relation -> {
              followRepository.removeRelation(relation);
              return profileResponse(
                  profileQueryService.findByUsername(username, currentUserId).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", ProfilePayload.from(profile));
          }
        });
  }
}
