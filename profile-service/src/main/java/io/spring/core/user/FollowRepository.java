package io.spring.core.user;

import java.util.Optional;

/** Write side of the social graph owned by profile-service. */
public interface FollowRepository {
  void saveRelation(FollowRelation followRelation);

  Optional<FollowRelation> findRelation(String userId, String targetId);

  void removeRelation(FollowRelation followRelation);
}
