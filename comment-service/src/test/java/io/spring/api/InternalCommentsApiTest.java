package io.spring.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.JacksonCustomizations;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.security.JwtVerifier;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InternalCommentsApi.class)
@Import(JacksonCustomizations.class)
@AutoConfigureMockMvc(addFilters = false)
public class InternalCommentsApiTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private CommentRepository commentRepository;
  @MockBean private JwtVerifier jwtVerifier;

  private final CommentData commentData =
      new CommentData(
          "comment-1",
          "body",
          "article-1",
          "user-1",
          new DateTime(),
          new DateTime(),
          new ProfileData("user-1", "johndoe", "bio", "image", true));

  @Test
  public void should_serve_comments_of_article() throws Exception {
    Mockito.when(commentQueryService.findByArticleId("article-1", "user-2"))
        .thenReturn(List.of(commentData));

    mockMvc
        .perform(get("/internal/articles/article-1/comments?currentUserId=user-2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("comment-1"))
        .andExpect(jsonPath("$[0].body").value("body"))
        .andExpect(jsonPath("$[0].author.username").value("johndoe"))
        .andExpect(jsonPath("$[0].author.following").value(true))
        .andExpect(jsonPath("$[0].articleId").doesNotExist());
  }

  @Test
  public void should_serve_comment_count() throws Exception {
    Mockito.when(commentQueryService.countByArticleId("article-1")).thenReturn(3);

    mockMvc
        .perform(get("/internal/articles/article-1/comments/count"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(3));
  }

  @Test
  public void should_delete_comment() throws Exception {
    Comment comment = new Comment("body", "user-1", "article-1");
    Mockito.when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

    mockMvc
        .perform(delete("/internal/comments/" + comment.getId()))
        .andExpect(status().isNoContent());
    Mockito.verify(commentRepository).remove(comment);
  }

  @Test
  public void should_return_404_for_unknown_comment() throws Exception {
    Mockito.when(commentRepository.findById("nope")).thenReturn(Optional.empty());
    Mockito.when(commentQueryService.findById("nope", null)).thenReturn(Optional.empty());

    mockMvc.perform(delete("/internal/comments/nope")).andExpect(status().isNotFound());
    mockMvc.perform(get("/internal/comments/nope")).andExpect(status().isNotFound());
  }
}
