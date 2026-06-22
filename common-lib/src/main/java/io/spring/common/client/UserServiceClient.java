package io.spring.common.client;

import io.spring.common.dto.ProfileData;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:8081}")
public interface UserServiceClient {

  @GetMapping("/internal/users/{id}/profile")
  ProfileData getProfileById(@PathVariable("id") String userId);

  @GetMapping("/internal/users/by-username/{username}/profile")
  ProfileData getProfileByUsername(@PathVariable("username") String username);

  @GetMapping("/internal/users/{userId}/is-following/{targetId}")
  boolean isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId);

  @PostMapping("/internal/users/{userId}/following-authors")
  Set<String> followingAuthors(
      @PathVariable("userId") String userId, @RequestBody List<String> authorIds);

  @GetMapping("/internal/users/{userId}/followed-users")
  List<String> followedUsers(@PathVariable("userId") String userId);
}
