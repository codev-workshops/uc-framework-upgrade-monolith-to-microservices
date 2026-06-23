package io.spring.commentservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.commentservice.JacksonCustomizations;
import io.spring.commentservice.application.CommentQueryService;
import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import io.spring.commentservice.security.WebSecurityConfig;
import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.dto.ArticleData;
import io.spring.shared.dto.UserData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsApiTest extends TestWithCurrentUser {

  @MockBean private ArticleServiceClient articleServiceClient;

  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;

  private ArticleData article;
  private CommentData commentData;
  private Comment comment;
  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    article =
        new ArticleData(
            "article-id-1",
            "title",
            "title",
            "desc",
            "body",
            false,
            0,
            null,
            null,
            Arrays.asList("test", "java"),
            new io.spring.shared.dto.ProfileData(
                userData.getId(), userData.getUsername(), userData.getBio(),
                userData.getImage(), false));
    when(articleServiceClient.getArticleBySlug(eq(article.getSlug())))
        .thenReturn(Optional.of(article));
    comment = new Comment("comment", userData.getId(), article.getId());
    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            new ProfileData(
                userData.getId(), userData.getUsername(), userData.getBio(),
                userData.getImage(), false));
  }

  @Test
  public void should_create_comment_success() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "comment content");
                  }
                });
          }
        };

    when(commentQueryService.findById(anyString(), eq(userData)))
        .thenReturn(Optional.of(commentData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(201)
        .body("comment.body", equalTo(commentData.getBody()));
  }

  @Test
  public void should_get_422_with_empty_body() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "");
                  }
                });
          }
        };

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_comments_of_article_success() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Arrays.asList(commentData));
    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()));
  }

  @Test
  public void should_delete_comment_success() throws Exception {
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .then()
        .statusCode(204);
  }

  @Test
  public void should_get_403_if_not_author_of_article_or_author_of_comment_when_delete_comment()
      throws Exception {
    UserData anotherUser = new UserData("another-user-id", "other@example.com", "other", "", "");
    when(userServiceClient.getUserById(eq(anotherUser.getId())))
        .thenReturn(Optional.of(anotherUser));
    when(jwtUtils.getSubFromToken(any())).thenReturn(Optional.of(anotherUser.getId()));

    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .then()
        .statusCode(403);
  }
}
