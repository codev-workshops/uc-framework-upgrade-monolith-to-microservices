package io.spring.userservice.api;

import io.spring.common.dto.ProfileData;
import io.spring.common.dto.UserData;
import io.spring.userservice.infrastructure.mybatis.readservice.UserReadService;
import io.spring.userservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{id}/profile")
  public ResponseEntity<ProfileData> getProfileById(@PathVariable("id") String userId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
    return ResponseEntity.ok(profileData);
  }

  @GetMapping("/by-username/{username}/profile")
  public ResponseEntity<ProfileData> getProfileByUsername(
      @PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
    return ResponseEntity.ok(profileData);
  }

  @GetMapping("/{userId}/is-following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @PostMapping("/{userId}/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @PathVariable("userId") String userId, @RequestBody List<String> authorIds) {
    return ResponseEntity.ok(userRelationshipQueryService.followingAuthors(userId, authorIds));
  }

  @GetMapping("/{userId}/followed-users")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    return ResponseEntity.ok(userRelationshipQueryService.followedUsers(userId));
  }
}
