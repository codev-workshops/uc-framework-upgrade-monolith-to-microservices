package io.spring.userservice.api.internal;

import io.spring.common.data.UserData;
import io.spring.userservice.domain.User;
import io.spring.userservice.domain.UserRepository;
import io.spring.userservice.infrastructure.mybatis.readservice.UserReadService;
import io.spring.userservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.userservice.service.JwtService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRepository userRepository;
  private JwtService jwtService;
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

  @PostMapping("/auth/validate")
  public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    if (token == null) {
      return ResponseEntity.badRequest().build();
    }
    Optional<String> subOpt = jwtService.getSubFromToken(token);
    if (subOpt.isEmpty()) {
      return ResponseEntity.status(401).build();
    }
    Optional<User> userOpt = userRepository.findById(subOpt.get());
    if (userOpt.isEmpty()) {
      return ResponseEntity.status(401).build();
    }
    User user = userOpt.get();
    Map<String, Object> result = new HashMap<>();
    result.put("id", user.getId());
    result.put("username", user.getUsername());
    result.put("email", user.getEmail());
    result.put("bio", user.getBio());
    result.put("image", user.getImage());
    return ResponseEntity.ok(result);
  }

  @GetMapping("/users/{id}/following")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("id") String id, @RequestParam("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(id, targetId);
    return ResponseEntity.ok(following);
  }

  @GetMapping("/users/{id}/followed-users")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable("id") String id) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(id);
    return ResponseEntity.ok(followedUsers);
  }

  @GetMapping("/users/following-authors")
  public ResponseEntity<Set<String>> getFollowingAuthors(
      @RequestParam("userId") String userId,
      @RequestParam("authorIds") List<String> authorIds) {
    Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(followingAuthors);
  }
}
