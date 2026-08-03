package io.spring.contracts.client;

import io.spring.contracts.dto.ProfileData;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Contract for resolving social-graph (follow) state and full profiles from profile-service. */
public interface ProfileServiceClient {
  /** Full profile (base user data + following flag relative to the current user, may be null). */
  Optional<ProfileData> findProfile(String username, String currentUserId);

  /** Whether {@code currentUserId} follows {@code targetUserId}. */
  boolean isFollowing(String currentUserId, String targetUserId);

  /** Subset of {@code authorIds} that {@code currentUserId} follows (batch read composition). */
  Set<String> followingAuthors(String currentUserId, List<String> authorIds);

  /** User ids that {@code userId} follows (used to build the personalized feed). */
  List<String> followedUsers(String userId);
}
