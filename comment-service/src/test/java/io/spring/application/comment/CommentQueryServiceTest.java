package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.UserSummary;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({CommentQueryService.class, MyBatisCommentRepository.class})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentQueryService commentQueryService;

  @MockBean private UserServiceClient userServiceClient;
  @MockBean private ProfileServiceClient profileServiceClient;

  private final UserSummary author = new UserSummary("user-1", "aisensiy", "bio", "image");
  private final UserSummary other = new UserSummary("user-2", "user2", "bio2", "image2");

  @BeforeEach
  public void setUp() {
    Mockito.when(userServiceClient.findByIds(ArgumentMatchers.anyCollection()))
        .thenReturn(List.of(author, other));
    Mockito.when(
            profileServiceClient.followingAuthors(
                ArgumentMatchers.eq("user-1"), ArgumentMatchers.anyList()))
        .thenReturn(Set.of("user-2"));
  }

  @Test
  public void should_read_comment_with_author_resolved_from_user_service() {
    Comment comment = new Comment("content", "user-1", "article-1");
    commentRepository.save(comment);

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), "user-1");
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals("aisensiy", commentData.getProfileData().getUsername());
    Assertions.assertFalse(commentData.getProfileData().isFollowing());
    Assertions.assertNotNull(commentData.getCreatedAt());
  }

  @Test
  public void should_read_comments_of_article_with_following_flag() {
    commentRepository.save(new Comment("content1", "user-1", "article-1"));
    commentRepository.save(new Comment("content2", "user-2", "article-1"));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", "user-1");
    Assertions.assertEquals(2, comments.size());
    Assertions.assertEquals(2, commentQueryService.countByArticleId("article-1"));
    Assertions.assertTrue(
        comments.stream()
            .anyMatch(
                c ->
                    "user-2".equals(c.getProfileData().getId())
                        && c.getProfileData().isFollowing()));
    Assertions.assertTrue(
        comments.stream()
            .anyMatch(
                c ->
                    "user-1".equals(c.getProfileData().getId())
                        && !c.getProfileData().isFollowing()));
  }

  @Test
  public void should_not_resolve_following_for_anonymous_reader() {
    commentRepository.save(new Comment("content", "user-2", "article-2"));

    List<CommentData> comments = commentQueryService.findByArticleId("article-2", null);
    Assertions.assertEquals(1, comments.size());
    Assertions.assertFalse(comments.get(0).getProfileData().isFollowing());
    Mockito.verify(profileServiceClient, Mockito.never())
        .followingAuthors(ArgumentMatchers.any(), ArgumentMatchers.anyList());
  }

  @Test
  public void should_return_empty_for_unknown_comment() {
    Assertions.assertFalse(commentQueryService.findById("not-exist", "user-1").isPresent());
    Assertions.assertEquals(0, commentQueryService.countByArticleId("no-article"));
  }
}
