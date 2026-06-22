package io.spring.integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentsApiIntegrationTest extends TestBase {

  private String token;
  private String articleSlug;
  private String commentId;

  @BeforeAll
  void setupArticle() {
    token = registerAndGetToken("commenttest", "commenttest@test.com", "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Article for Comments Test");
    article.put("description", "Test comments");
    article.put("body", "Body");
    article.put("tagList", List.of("comments"));
    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    articleSlug =
        authenticatedRequest(token)
            .body(body)
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(1)
  void createComment_returnsCommentData() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "This is a test comment");
    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    commentId =
        authenticatedRequest(token)
            .body(body)
            .when()
            .post("/articles/" + articleSlug + "/comments")
            .then()
            .statusCode(201)
            .body("comment.body", equalTo("This is a test comment"))
            .body("comment.author.username", equalTo("commenttest"))
            .extract()
            .path("comment.id");
  }

  @Test
  @Order(2)
  void listComments_returnsComments() {
    given()
        .contentType("application/json")
        .when()
        .get("/articles/" + articleSlug + "/comments")
        .then()
        .statusCode(200)
        .body("comments.size()", greaterThanOrEqualTo(1));
  }

  @Test
  @Order(3)
  void deleteComment_returns204() {
    authenticatedRequest(token)
        .when()
        .delete("/articles/" + articleSlug + "/comments/" + commentId)
        .then()
        .statusCode(204);
  }
}
