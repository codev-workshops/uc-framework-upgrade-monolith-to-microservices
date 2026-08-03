package io.spring.application.profile;

import io.spring.application.ProfileQueryService;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.FollowRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisFollowRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({ProfileQueryService.class, MyBatisFollowRepository.class})
public class ProfileQueryServiceTest extends DbTestBase {
  @Autowired private ProfileQueryService profileQueryService;
  @Autowired private FollowRepository followRepository;
  @MockBean private UserServiceClient userServiceClient;

  private final UserSummary profileUser = new UserSummary("user-p", "p", "bio", "image");

  @BeforeEach
  public void setUp() {
    Mockito.when(userServiceClient.findByUsername("p")).thenReturn(Optional.of(profileUser));
    Mockito.when(userServiceClient.findByUsername("missing")).thenReturn(Optional.empty());
  }

  @Test
  public void should_fetch_profile_success() {
    Optional<ProfileData> optional = profileQueryService.findByUsername("p", "user-a");
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals("p", optional.get().getUsername());
    Assertions.assertEquals("bio", optional.get().getBio());
    Assertions.assertFalse(optional.get().isFollowing());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    Assertions.assertFalse(profileQueryService.findByUsername("missing", "user-a").isPresent());
  }

  @Test
  public void should_mark_following_when_relation_exists() {
    followRepository.saveRelation(new FollowRelation("user-a", "user-p"));

    Optional<ProfileData> optional = profileQueryService.findByUsername("p", "user-a");
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertTrue(optional.get().isFollowing());
  }

  @Test
  public void should_not_mark_following_for_anonymous_user() {
    followRepository.saveRelation(new FollowRelation("user-a", "user-p"));

    Optional<ProfileData> optional = profileQueryService.findByUsername("p", null);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(optional.get().isFollowing());
  }

  @Test
  public void should_read_following_authors_and_followed_users() {
    followRepository.saveRelation(new FollowRelation("user-a", "user-p"));
    followRepository.saveRelation(new FollowRelation("user-a", "user-q"));

    Set<String> followingAuthors =
        profileQueryService.followingAuthors("user-a", List.of("user-p", "user-z"));
    Assertions.assertEquals(Set.of("user-p"), followingAuthors);
    Assertions.assertEquals(Set.of(), profileQueryService.followingAuthors("user-a", List.of()));
    Assertions.assertEquals(
        Set.of("user-p", "user-q"), Set.copyOf(profileQueryService.followedUsers("user-a")));
    Assertions.assertTrue(profileQueryService.isFollowing("user-a", "user-p"));
    Assertions.assertFalse(profileQueryService.isFollowing("user-a", "user-z"));
  }

  @Test
  public void should_remove_relation() {
    FollowRelation relation = new FollowRelation("user-a", "user-p");
    followRepository.saveRelation(relation);
    Assertions.assertTrue(followRepository.findRelation("user-a", "user-p").isPresent());

    followRepository.removeRelation(relation);
    Assertions.assertFalse(followRepository.findRelation("user-a", "user-p").isPresent());
    Assertions.assertFalse(profileQueryService.isFollowing("user-a", "user-p"));
  }
}
