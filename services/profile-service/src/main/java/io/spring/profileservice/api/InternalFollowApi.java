package io.spring.profileservice.api;

import io.spring.profileservice.infrastructure.mybatis.readservice.FollowsQueryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service follow-graph queries consumed by article feed + comment enrichment (see
 * contracts/openapi/profile-service.yaml). NOT exposed by the gateway.
 */
@RestController
@RequestMapping(path = "internal/follows")
@AllArgsConstructor
public class InternalFollowApi {
  private final FollowsQueryService followsQueryService;

  @GetMapping
  public ResponseEntity<Map<String, Boolean>> isFollowing(
      @RequestParam("userId") String userId, @RequestParam("targetId") String targetId) {
    Map<String, Boolean> body = new HashMap<>();
    body.put("following", followsQueryService.isUserFollowing(userId, targetId));
    return ResponseEntity.ok(body);
  }

  @GetMapping(path = "followed")
  public ResponseEntity<List<String>> followed(@RequestParam("userId") String userId) {
    return ResponseEntity.ok(followsQueryService.followedUsers(userId));
  }

  @PostMapping(path = "among")
  public ResponseEntity<Set<String>> among(
      @RequestParam("userId") String userId, @RequestBody List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return ResponseEntity.ok(Collections.emptySet());
    }
    return ResponseEntity.ok(followsQueryService.followingAuthors(userId, ids));
  }
}
