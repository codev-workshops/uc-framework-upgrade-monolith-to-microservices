package io.spring.api;

import io.spring.application.ProfileQueryService;
import io.spring.contracts.dto.ProfileData;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inter-service endpoints backing the {@code ProfileServiceClient} contract: social-graph state and
 * composed profiles for read composition in other services.
 */
@RestController
@RequestMapping(path = "/internal")
@AllArgsConstructor
public class InternalProfilesApi {

  private final ProfileQueryService profileQueryService;

  @GetMapping("/profiles/{username}")
  public ResponseEntity<ProfileData> findProfile(
      @PathVariable("username") String username,
      @RequestParam(value = "currentUserId", required = false) String currentUserId) {
    return profileQueryService
        .findByUsername(username, currentUserId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/follows/is-following")
  public ResponseEntity<Boolean> isFollowing(
      @RequestParam("currentUserId") String currentUserId,
      @RequestParam("targetUserId") String targetUserId) {
    return ResponseEntity.ok(profileQueryService.isFollowing(currentUserId, targetUserId));
  }

  @GetMapping("/follows/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @RequestParam("currentUserId") String currentUserId,
      @RequestParam("authorIds") List<String> authorIds) {
    return ResponseEntity.ok(profileQueryService.followingAuthors(currentUserId, authorIds));
  }

  @GetMapping("/follows/followed-users")
  public ResponseEntity<List<String>> followedUsers(@RequestParam("userId") String userId) {
    return ResponseEntity.ok(profileQueryService.followedUsers(userId));
  }
}
