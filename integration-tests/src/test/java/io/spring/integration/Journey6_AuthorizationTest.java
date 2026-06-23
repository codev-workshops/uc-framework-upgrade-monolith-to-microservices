package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 6: Authorization
 *
 * <ul>
 *   <li>User2 tries to DELETE user1's article → assert 403
 *   <li>User2 tries to DELETE user1's comment → assert 403
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey6_AuthorizationTest extends IntegrationTestBase {

  private static String user1Jwt;
  private static String user2Jwt;
  private static String slug;
  private static String commentId;

  @BeforeAll
  static void setup() {
    user1Jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\"authzuser1\",\"email\":\"authz1@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    user2Jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\"authzuser2\",\"email\":\"authz2@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    slug =
        givenAuth(user1Jwt)
            .body(
                "{\"article\":{\"title\":\"AuthZ Article\","
                    + "\"description\":\"Authorization test\","
                    + "\"body\":\"Content\","
                    + "\"tagList\":[\"authz\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(201)
            .extract()
            .path("article.slug");

    commentId =
        givenAuth(user1Jwt)
            .body("{\"comment\":{\"body\":\"User1 comment\"}}")
            .when()
            .post("/articles/" + slug + "/comments")
            .then()
            .statusCode(201)
            .extract()
            .path("comment.id");
  }

  @Test
  @Order(1)
  void user2CannotDeleteUser1Article() {
    givenAuth(user2Jwt).when().delete("/articles/" + slug).then().statusCode(403);
  }

  @Test
  @Order(2)
  void user2CannotDeleteUser1Comment() {
    givenAuth(user2Jwt)
        .when()
        .delete("/articles/" + slug + "/comments/" + commentId)
        .then()
        .statusCode(403);
  }
}
