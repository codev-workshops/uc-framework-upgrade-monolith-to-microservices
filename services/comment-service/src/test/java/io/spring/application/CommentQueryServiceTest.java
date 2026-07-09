package io.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.clients.ProfileClient;
import io.spring.application.clients.UserAuthClient;
import io.spring.application.data.AuthorRef;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserAuthClient userAuthClient;
  @Mock private ProfileClient profileClient;

  @InjectMocks private CommentQueryService commentQueryService;

  private CommentData c1;
  private CommentData c2;

  @BeforeEach
  void setUp() {
    c1 = comment("comment-1", "user-2");
    c2 = comment("comment-2", "user-3");
  }

  private CommentData comment(String id, String authorId) {
    ProfileData profile = new ProfileData();
    profile.setId(authorId);
    return new CommentData(id, "body " + id, "article-1", new DateTime(), new DateTime(), profile);
  }

  @Test
  void should_enrich_author_projection_from_user_auth() {
    when(commentReadService.findByArticleId(eq("article-1")))
        .thenReturn(Collections.singletonList(c1));
    when(userAuthClient.findById(eq("user-2"), any()))
        .thenReturn(Optional.of(new AuthorRef("user-2", "jake", "bio", "img")));

    List<CommentData> result = commentQueryService.findByArticleId("article-1", null, "Bearer t");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProfileData().getUsername()).isEqualTo("jake");
    assertThat(result.get(0).getProfileData().getBio()).isEqualTo("bio");
    assertThat(result.get(0).getProfileData().isFollowing()).isFalse();
  }

  @Test
  void should_set_following_flag_for_followed_authors() {
    when(commentReadService.findByArticleId(eq("article-1"))).thenReturn(Arrays.asList(c1, c2));
    when(userAuthClient.findById(any(), any()))
        .thenAnswer(inv -> Optional.of(new AuthorRef(inv.getArgument(0), "name", "bio", "img")));
    when(profileClient.followingAmong(eq("user-1"), any(), any()))
        .thenReturn(new HashSet<>(Collections.singletonList("user-2")));

    List<CommentData> result =
        commentQueryService.findByArticleId("article-1", "user-1", "Bearer t");

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getProfileData().isFollowing()).isTrue();
    assertThat(result.get(1).getProfileData().isFollowing()).isFalse();
  }

  @Test
  void should_not_call_profile_when_anonymous() {
    when(commentReadService.findByArticleId(eq("article-1")))
        .thenReturn(Collections.singletonList(c1));
    when(userAuthClient.findById(any(), any()))
        .thenReturn(Optional.of(new AuthorRef("user-2", "jake", "bio", "img")));

    commentQueryService.findByArticleId("article-1", null, null);

    org.mockito.Mockito.verifyNoInteractions(profileClient);
  }

  @Test
  void should_return_empty_when_comment_missing() {
    when(commentReadService.findById(eq("missing"))).thenReturn(null);
    assertThat(commentQueryService.findById("missing", "user-1", "Bearer t")).isEmpty();
  }

  @Test
  void should_find_by_id_and_enrich() {
    when(commentReadService.findById(eq("comment-1"))).thenReturn(c1);
    when(userAuthClient.findById(eq("user-2"), any()))
        .thenReturn(Optional.of(new AuthorRef("user-2", "jake", "bio", "img")));
    when(profileClient.followingAmong(eq("user-1"), any(), any()))
        .thenReturn(new HashSet<>(Collections.singletonList("user-2")));

    Optional<CommentData> result = commentQueryService.findById("comment-1", "user-1", "Bearer t");

    assertThat(result).isPresent();
    assertThat(result.get().getProfileData().getUsername()).isEqualTo("jake");
    assertThat(result.get().getProfileData().isFollowing()).isTrue();
  }
}
