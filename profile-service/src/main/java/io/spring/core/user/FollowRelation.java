package io.spring.core.user;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Directed social-graph edge: {@code userId} follows {@code targetId}. */
@NoArgsConstructor
@Data
public class FollowRelation {
  private String userId;
  private String targetId;

  public FollowRelation(String userId, String targetId) {
    this.userId = userId;
    this.targetId = targetId;
  }
}
