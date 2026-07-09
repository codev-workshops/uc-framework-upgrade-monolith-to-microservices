package io.spring.profileservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.profileservice.core.follow.FollowRelation;
import io.spring.profileservice.core.follow.FollowRepository;
import io.spring.profileservice.infrastructure.mybatis.readservice.FollowsQueryService;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test against a real SQLite datasource with Flyway-applied migrations (schema + seed
 * from contracts/migrations/profile-service). Runs under the `mock` profile so the upstream
 * user-auth client is stubbed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"test", "mock"})
public class MyBatisFollowRepositoryIntegrationTest {

  @Autowired private FollowRepository followRepository;
  @Autowired private FollowsQueryService followsQueryService;

  @Test
  public void seeded_relationships_are_queryable() {
    assertThat(followsQueryService.isUserFollowing("user-1", "user-2")).isTrue();
    assertThat(followsQueryService.isUserFollowing("user-2", "user-3")).isFalse();
  }

  @Test
  public void followed_users_returns_targets() {
    assertThat(followsQueryService.followedUsers("user-3"))
        .containsExactlyInAnyOrder("user-1", "user-2");
  }

  @Test
  public void following_authors_returns_subset() {
    assertThat(followsQueryService.followingAuthors("user-3", Arrays.asList("user-1", "user-9")))
        .containsExactly("user-1");
  }

  @Test
  public void save_and_remove_relation_round_trip() {
    FollowRelation relation = new FollowRelation("user-2", "user-3");
    followRepository.saveRelation(relation);
    assertThat(followRepository.findRelation("user-2", "user-3")).isPresent();
    assertThat(followsQueryService.isUserFollowing("user-2", "user-3")).isTrue();

    followRepository.removeRelation(relation);
    assertThat(followRepository.findRelation("user-2", "user-3")).isNotPresent();
  }

  @Test
  public void save_relation_is_idempotent() {
    FollowRelation relation = new FollowRelation("user-1", "user-3");
    followRepository.saveRelation(relation);
    followRepository.saveRelation(relation);
    assertThat(followRepository.findRelation("user-1", "user-3")).isPresent();
    followRepository.removeRelation(relation);
  }
}
