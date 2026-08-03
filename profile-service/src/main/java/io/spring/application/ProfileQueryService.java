package io.spring.application;

import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds profiles by composing base user data resolved from user-service with the {@code following}
 * flag read from the {@code follows} table owned by this service.
 */
@Component
@AllArgsConstructor
public class ProfileQueryService {
  private final UserServiceClient userServiceClient;
  private final UserRelationshipQueryService userRelationshipQueryService;

  public Optional<ProfileData> findByUsername(String username, String currentUserId) {
    return userServiceClient.findByUsername(username).map(user -> toProfile(user, currentUserId));
  }

  public Optional<ProfileData> findById(String id, String currentUserId) {
    return userServiceClient.findById(id).map(user -> toProfile(user, currentUserId));
  }

  public boolean isFollowing(String currentUserId, String targetUserId) {
    return currentUserId != null
        && targetUserId != null
        && userRelationshipQueryService.isUserFollowing(currentUserId, targetUserId);
  }

  public Set<String> followingAuthors(String currentUserId, List<String> authorIds) {
    if (currentUserId == null || authorIds == null || authorIds.isEmpty()) {
      return Set.of();
    }
    return userRelationshipQueryService.followingAuthors(currentUserId, authorIds);
  }

  public List<String> followedUsers(String userId) {
    if (userId == null) {
      return List.of();
    }
    return userRelationshipQueryService.followedUsers(userId);
  }

  private ProfileData toProfile(UserSummary user, String currentUserId) {
    return new ProfileData(
        user.getId(),
        user.getUsername(),
        user.getBio(),
        user.getImage(),
        isFollowing(currentUserId, user.getId()));
  }
}
