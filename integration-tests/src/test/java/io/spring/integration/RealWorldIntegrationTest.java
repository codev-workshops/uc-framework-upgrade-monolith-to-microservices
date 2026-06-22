package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldIntegrationTest {

  private static final String BASE_URL = "http://localhost:8080/api";
  private static String userToken;
  private static String user2Token;
  private static String articleSlug;
  private static String commentId;

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = BASE_URL;
  }

  @Test
  @Order(1)
  void registerUser() {
    userToken =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"user\":{\"email\":\"test@test.com\",\"username\":\"testuser\",\"password\":\"password123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .body("user.username", equalTo("testuser"))
            .body("user.email", equalTo("test@test.com"))
            .body("user.token", notNullValue())
            .extract()
            .path("user.token");
  }

  @Test
  @Order(2)
  void loginUser() {
    userToken =
        given()
            .contentType(ContentType.JSON)
            .body("{\"user\":{\"email\":\"test@test.com\",\"password\":\"password123\"}}")
            .when()
            .post("/users/login")
            .then()
            .statusCode(200)
            .body("user.token", notNullValue())
            .extract()
            .path("user.token");
  }

  @Test
  @Order(3)
  void getCurrentUser() {
    given()
        .header("Authorization", "Token " + userToken)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.username", equalTo("testuser"))
        .body("user.email", equalTo("test@test.com"));
  }

  @Test
  @Order(4)
  void updateUser() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + userToken)
        .body("{\"user\":{\"bio\":\"I work at StateFactory\"}}")
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("I work at StateFactory"));
  }

  @Test
  @Order(5)
  void getProfile() {
    given()
        .when()
        .get("/profiles/testuser")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo("testuser"))
        .body("profile.following", equalTo(false));
  }

  @Test
  @Order(6)
  void registerUser2() {
    user2Token =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"user\":{\"email\":\"test2@test.com\",\"username\":\"testuser2\",\"password\":\"password123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");
  }

  @Test
  @Order(7)
  void followUser() {
    given()
        .header("Authorization", "Token " + user2Token)
        .when()
        .post("/profiles/testuser/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(8)
  void unfollowUser() {
    given()
        .header("Authorization", "Token " + user2Token)
        .when()
        .delete("/profiles/testuser/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));
  }

  @Test
  @Order(9)
  void createArticle() {
    articleSlug =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + userToken)
            .body(
                "{\"article\":{\"title\":\"How to train your dragon\",\"description\":\"Ever wonder how?\",\"body\":\"You have to believe\",\"tagList\":[\"reactjs\",\"angularjs\",\"dragons\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .body("article.title", equalTo("How to train your dragon"))
            .body("article.tagList", hasItems("reactjs", "angularjs", "dragons"))
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(10)
  void getArticle() {
    given()
        .when()
        .get("/articles/" + articleSlug)
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(articleSlug))
        .body("article.author.username", equalTo("testuser"));
  }

  @Test
  @Order(11)
  void updateArticle() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + userToken)
        .body("{\"article\":{\"title\":\"Did you train your dragon?\"}}")
        .when()
        .put("/articles/" + articleSlug)
        .then()
        .statusCode(200)
        .body("article.title", equalTo("Did you train your dragon?"));

    articleSlug = "did-you-train-your-dragon?";
  }

  @Test
  @Order(12)
  void listArticles() {
    given()
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", greaterThanOrEqualTo(1));
  }

  @Test
  @Order(13)
  void getTags() {
    given()
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags", hasItems("reactjs", "angularjs", "dragons"));
  }

  @Test
  @Order(14)
  void createComment() {
    commentId =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + userToken)
            .body("{\"comment\":{\"body\":\"His name was my name too.\"}}")
            .when()
            .post("/articles/" + articleSlug + "/comments")
            .then()
            .statusCode(201)
            .body("comment.body", equalTo("His name was my name too."))
            .extract()
            .path("comment.id");
  }

  @Test
  @Order(15)
  void listComments() {
    given()
        .when()
        .get("/articles/" + articleSlug + "/comments")
        .then()
        .statusCode(200)
        .body("comments", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(16)
  void deleteComment() {
    given()
        .header("Authorization", "Token " + userToken)
        .when()
        .delete("/articles/" + articleSlug + "/comments/" + commentId)
        .then()
        .statusCode(204);
  }

  @Test
  @Order(17)
  void favoriteArticle() {
    given()
        .header("Authorization", "Token " + userToken)
        .when()
        .post("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(18)
  void unfavoriteArticle() {
    given()
        .header("Authorization", "Token " + userToken)
        .when()
        .delete("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(false))
        .body("article.favoritesCount", equalTo(0));
  }

  @Test
  @Order(19)
  void feedTest() {
    // User2 follows User1
    given()
        .header("Authorization", "Token " + user2Token)
        .when()
        .post("/profiles/testuser/follow")
        .then()
        .statusCode(200);

    // User2 checks feed
    given()
        .header("Authorization", "Token " + user2Token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articlesCount", greaterThanOrEqualTo(1));
  }
}
