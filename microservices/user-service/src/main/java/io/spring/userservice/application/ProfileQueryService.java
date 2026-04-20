package io.spring.userservice.application;

import io.spring.userservice.application.data.ProfileData;
import io.spring.userservice.application.data.UserData;
import io.spring.userservice.infrastructure.mybatis.readservice.UserReadService;
import io.spring.userservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
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
                  && userRelationshipQueryService.isUserFollowing(currentUserId, userData.getId()));
      return Optional.of(profileData);
    }
  }
}
