package io.spring.userservice.api;

import io.spring.userservice.application.data.UserData;
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
@RequestMapping(path = "/internal")
@AllArgsConstructor
public class InternalUserApi {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/users/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-username/{username}")
  public ResponseEntity<UserData> getUserByUsername(@PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/follows/{userId}/is-following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @PostMapping("/follows/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @RequestBody FollowingAuthorsRequest request) {
    return ResponseEntity.ok(
        userRelationshipQueryService.followingAuthors(request.getUserId(), request.getAuthorIds()));
  }

  @GetMapping("/follows/{userId}/followed-users")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    return ResponseEntity.ok(userRelationshipQueryService.followedUsers(userId));
  }
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class FollowingAuthorsRequest {
  private String userId;
  private List<String> authorIds;
}
