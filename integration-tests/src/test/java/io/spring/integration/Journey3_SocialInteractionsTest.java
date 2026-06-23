package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 3: Social Interactions
 *
 * <ul>
 *   <li>Register a second user
 *   <li>POST /profiles/{user1}/follow (user2 follows user1)
 *   <li>User1 creates an article
 *   <li>GET /articles/feed (as user2) → assert user1's article appears
 *   <li>DELETE /profiles/{user1}/follow → feed should be empty
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey3_SocialInteractionsTest extends IntegrationTestBase {

  private static String user1Jwt;
  private static String user2Jwt;
  private static final String USER1 = "socialuser1";
  private static final String USER2 = "socialuser2";

  @BeforeAll
  static void registerUsers() {
    user1Jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\""
                    + USER1
                    + "\",\"email\":\"social1@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    user2Jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\""
                    + USER2
                    + "\",\"email\":\"social2@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");
  }

  @Test
  @Order(1)
  void user2FollowsUser1() {
    givenAuth(user2Jwt)
        .when()
        .post("/profiles/" + USER1 + "/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(2)
  void user1CreatesArticle() {
    givenAuth(user1Jwt)
        .body(
            "{\"article\":{\"title\":\"Social Article\","
                + "\"description\":\"For feed test\","
                + "\"body\":\"Content here\","
                + "\"tagList\":[\"social\"]}}")
        .when()
        .post("/articles")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(3)
  void user2FeedShowsUser1Article() {
    givenAuth(user2Jwt)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articlesCount", greaterThanOrEqualTo(1))
        .body("articles[0].author.username", equalTo(USER1));
  }

  @Test
  @Order(4)
  void user2UnfollowsUser1() {
    givenAuth(user2Jwt)
        .when()
        .delete("/profiles/" + USER1 + "/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));
  }

  @Test
  @Order(5)
  void user2FeedIsEmptyAfterUnfollow() {
    givenAuth(user2Jwt)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }
}
