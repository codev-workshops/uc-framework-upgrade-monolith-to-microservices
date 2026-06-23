package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 5: Comments
 *
 * <ul>
 *   <li>POST /articles/{slug}/comments → assert 201
 *   <li>GET /articles/{slug}/comments → assert comment listed
 *   <li>DELETE /articles/{slug}/comments/{id} → assert 204
 *   <li>GET /articles/{slug}/comments → assert empty
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey5_CommentsTest extends IntegrationTestBase {

  private static String jwt;
  private static String slug;
  private static String commentId;

  @BeforeAll
  static void setupUserAndArticle() {
    jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\"commentuser\",\"email\":\"commentuser@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    slug =
        givenAuth(jwt)
            .body(
                "{\"article\":{\"title\":\"Comment Article\","
                    + "\"description\":\"For comments test\","
                    + "\"body\":\"Content\","
                    + "\"tagList\":[\"comments\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(201)
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(1)
  void addComment() {
    commentId =
        givenAuth(jwt)
            .body("{\"comment\":{\"body\":\"Great article!\"}}")
            .when()
            .post("/articles/" + slug + "/comments")
            .then()
            .statusCode(201)
            .body("comment.body", equalTo("Great article!"))
            .extract()
            .path("comment.id");
  }

  @Test
  @Order(2)
  void listComments() {
    givenJson()
        .when()
        .get("/articles/" + slug + "/comments")
        .then()
        .statusCode(200)
        .body("comments.size()", equalTo(1))
        .body("comments[0].body", equalTo("Great article!"));
  }

  @Test
  @Order(3)
  void deleteComment() {
    givenAuth(jwt)
        .when()
        .delete("/articles/" + slug + "/comments/" + commentId)
        .then()
        .statusCode(204);
  }

  @Test
  @Order(4)
  void listCommentsAfterDelete() {
    givenJson()
        .when()
        .get("/articles/" + slug + "/comments")
        .then()
        .statusCode(200)
        .body("comments.size()", equalTo(0));
  }
}
