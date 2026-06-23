package io.spring.shared.client;

import io.spring.shared.dto.ProfileData;
import io.spring.shared.dto.UserData;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client interface for calling the User Service's internal API. Implementations may use Feign or
 * RestTemplate.
 */
public interface UserServiceClient {

  /** Get user by ID. */
  Optional<UserData> getUserById(String id);

  /**
   * Check which target user IDs are followed by the given user.
   *
   * @return map of targetId → isFollowing
   */
  Map<String, Boolean> getFollowingAuthors(String userId, List<String> targetIds);

  /** Check if userId follows targetId. */
  boolean isFollowing(String userId, String targetId);

  /** Get profile data for a user, with following status relative to currentUserId. */
  Optional<ProfileData> getProfile(String userId, String currentUserId);
}
