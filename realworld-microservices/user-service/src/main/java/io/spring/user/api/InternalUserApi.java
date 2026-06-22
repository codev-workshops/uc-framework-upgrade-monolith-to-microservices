package io.spring.user.api;

import io.spring.shared.dto.UserData;
import io.spring.user.infrastructure.UserReadService;
import io.spring.user.infrastructure.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/by-ids")
  public ResponseEntity<List<UserData>> getUsersByIds(@RequestParam("ids") List<String> ids) {
    List<UserData> users =
        ids.stream()
            .map(id -> userReadService.findById(id))
            .filter(userData -> userData != null)
            .collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{userId}/following")
  public ResponseEntity<Set<String>> getFollowingStatus(
      @PathVariable("userId") String userId,
      @RequestParam("targetIds") List<String> targetIds) {
    Set<String> following = userRelationshipQueryService.followingAuthors(userId, targetIds);
    return ResponseEntity.ok(following != null ? following : new HashSet<>());
  }

  @GetMapping("/{userId}/followed-users")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers != null ? followedUsers : new ArrayList<>());
  }
}
