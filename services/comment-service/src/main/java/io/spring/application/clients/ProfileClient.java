package io.spring.application.clients;

import java.util.List;
import java.util.Set;

/** Resolves the follow-graph owned by profile-service. */
public interface ProfileClient {
  /**
   * Given a current {@code userId} and candidate author ids, return the subset that {@code userId}
   * follows via profile-service {@code POST /internal/follows/among?userId=}. Mirrors
   * UserRelationshipQueryService.followingAuthors.
   */
  Set<String> followingAmong(String userId, List<String> candidateIds, String authorization);
}
