package io.spring.userservice.api;

import io.spring.shared.data.ProfileData;
import io.spring.shared.data.UserData;
import io.spring.shared.exception.ResourceNotFoundException;
import io.spring.userservice.infrastructure.UserReadService;
import io.spring.userservice.infrastructure.UserRelationshipQueryService;
import io.spring.userservice.service.ProfileQueryService;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ProfileQueryService profileQueryService;

  @GetMapping("/users/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-username/{username}")
  public ResponseEntity<UserData> getUserByUsername(@PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/{userId}/following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @GetMapping("/users/{userId}/following-among")
  public ResponseEntity<Set<String>> followingAuthors(
      @PathVariable("userId") String userId, @RequestParam("ids") List<String> ids) {
    return ResponseEntity.ok(userRelationshipQueryService.followingAuthors(userId, ids));
  }

  @GetMapping("/users/{userId}/followed")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    return ResponseEntity.ok(userRelationshipQueryService.followedUsers(userId));
  }

  @GetMapping("/profiles/{username}")
  public ResponseEntity<ProfileData> getProfile(
      @PathVariable("username") String username,
      @RequestParam(value = "currentUserId", required = false) String currentUserId) {
    return profileQueryService
        .findByUsername(username, currentUserId)
        .map(ResponseEntity::ok)
        .orElseThrow(ResourceNotFoundException::new);
  }
}
