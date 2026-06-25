package io.spring.userservice.service;

import io.spring.shared.data.ProfileData;
import io.spring.shared.data.UserData;
import io.spring.userservice.infrastructure.UserReadService;
import io.spring.userservice.infrastructure.UserRelationshipQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<ProfileData> findByUsername(String username, String currentUserId) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    } else {
      ProfileData profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              currentUserId != null
                  && userRelationshipQueryService.isUserFollowing(
                      currentUserId, userData.getId()));
      return Optional.of(profileData);
    }
  }
}
