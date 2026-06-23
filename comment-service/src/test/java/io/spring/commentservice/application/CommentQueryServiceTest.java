package io.spring.commentservice.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.commentservice.infrastructure.repository.MyBatisCommentRepository;
import io.spring.commentservice.infrastructure.DbTestBase;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.UserData;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({MyBatisCommentRepository.class, CommentQueryService.class})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentQueryService commentQueryService;

  @MockBean private UserServiceClient userServiceClient;

  private UserData user;

  @BeforeEach
  public void setUp() {
    user = new UserData("user-1", "aisensiy@test.com", "aisensiy", "", "");
    when(userServiceClient.getUserById(eq(user.getId()))).thenReturn(Optional.of(user));
    when(userServiceClient.isFollowing(any(), any())).thenReturn(false);
    when(userServiceClient.getFollowingAuthors(any(), any()))
        .thenReturn(Collections.emptyMap());
  }

  @Test
  public void should_read_comment_success() {
    Comment comment = new Comment("content", user.getId(), "123");
    commentRepository.save(comment);

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    UserData user2 = new UserData("user-2", "user2@email.com", "user2", "", "");
    when(userServiceClient.getUserById(eq(user2.getId()))).thenReturn(Optional.of(user2));

    Map<String, Boolean> followingMap = new HashMap<>();
    followingMap.put(user2.getId(), true);
    when(userServiceClient.getFollowingAuthors(eq(user.getId()), any()))
        .thenReturn(followingMap);

    Comment comment1 = new Comment("content1", user.getId(), "article-1");
    commentRepository.save(comment1);
    Comment comment2 = new Comment("content2", user2.getId(), "article-1");
    commentRepository.save(comment2);

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
