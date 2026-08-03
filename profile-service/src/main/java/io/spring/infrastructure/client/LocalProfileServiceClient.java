package io.spring.infrastructure.client;

import io.spring.application.ProfileQueryService;
import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.dto.ProfileData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * In-process implementation of the {@code ProfileServiceClient} contract; the same operations are
 * exposed over HTTP by {@code InternalProfilesApi}.
 */
@Component
@AllArgsConstructor
public class LocalProfileServiceClient implements ProfileServiceClient {
  private final ProfileQueryService profileQueryService;

  @Override
  public Optional<ProfileData> findProfile(String username, String currentUserId) {
    return profileQueryService.findByUsername(username, currentUserId);
  }

  @Override
  public boolean isFollowing(String currentUserId, String targetUserId) {
    return profileQueryService.isFollowing(currentUserId, targetUserId);
  }

  @Override
  public Set<String> followingAuthors(String currentUserId, List<String> authorIds) {
    return profileQueryService.followingAuthors(currentUserId, authorIds);
  }

  @Override
  public List<String> followedUsers(String userId) {
    return profileQueryService.followedUsers(userId);
  }
}
