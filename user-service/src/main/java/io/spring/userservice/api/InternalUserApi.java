package io.spring.userservice.api;

import io.spring.shared.dto.UserData;
import io.spring.userservice.core.UserRepository;
import io.spring.userservice.infrastructure.mybatis.readservice.UserReadService;
import io.spring.userservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private UserRepository userRepository;

  @GetMapping("/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    io.spring.userservice.application.data.UserData localData = userReadService.findById(id);
    if (localData == null) {
      return ResponseEntity.notFound().build();
    }
    UserData dto =
        new UserData(
            localData.getId(),
            localData.getEmail(),
            localData.getUsername(),
            localData.getBio(),
            localData.getImage());
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/{userId}/following")
  public ResponseEntity<Map<String, Boolean>> getFollowingStatus(
      @PathVariable("userId") String userId,
      @RequestParam("targets") String targets) {
    List<String> targetIds = Arrays.asList(targets.split(","));
    Set<String> followingSet = userRelationshipQueryService.followingAuthors(userId, targetIds);
    Map<String, Boolean> result = new HashMap<>();
    for (String targetId : targetIds) {
      result.put(targetId, followingSet.contains(targetId));
    }
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{userId}/followed-users")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable("userId") String userId) {
    List<String> followedUserIds = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUserIds);
  }
}
