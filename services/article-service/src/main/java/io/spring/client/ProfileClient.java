package io.spring.client;

import java.util.List;

/** Read-only view of profile-service used to compose author.following and the feed. */
public interface ProfileClient {

  /**
   * {@code GET /internal/follows/followed?userId=} -> author ids the user follows (drives feed).
   */
  List<String> followedAuthors(String userId);

  /**
   * {@code POST /internal/follows/among?userId=} -> the subset of the candidate author ids the user
   * follows.
   */
  List<String> followingAmong(String userId, List<String> candidateAuthorIds);
}
