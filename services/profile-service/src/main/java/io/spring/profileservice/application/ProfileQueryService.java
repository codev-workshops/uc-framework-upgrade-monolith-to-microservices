package io.spring.profileservice.application;

import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.infrastructure.client.AuthorRef;
import io.spring.profileservice.infrastructure.client.UserAuthClient;
import io.spring.profileservice.infrastructure.mybatis.readservice.FollowsQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private final UserAuthClient userAuthClient;
  private final FollowsQueryService followsQueryService;

  public Optional<ProfileData> findByUsername(String username, String currentUserId) {
    return userAuthClient
        .findByUsername(username)
        .map(author -> toProfileData(author, currentUserId));
  }

  public ProfileData toProfileData(AuthorRef author, String currentUserId) {
    boolean following =
        currentUserId != null && followsQueryService.isUserFollowing(currentUserId, author.getId());
    return new ProfileData(
        author.getId(), author.getUsername(), author.getBio(), author.getImage(), following);
  }
}
