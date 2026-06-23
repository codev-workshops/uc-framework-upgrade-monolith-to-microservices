package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 7: Cross-Service Data Integrity
 *
 * <ul>
 *   <li>Create article, favorite it, comment on it
 *   <li>GET /articles/{slug} → verify it includes favoritesCount, favorited flag, author profile
 *       with following status — data aggregated from 3 services
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey7_CrossServiceDataIntegrityTest extends IntegrationTestBase {

  private static String authorJwt;
  private static String readerJwt;
  private static String slug;
  private static final String AUTHOR = "crossauthor";
  private static final String READER = "crossreader";

  @BeforeAll
  static void setupUsersAndArticle() {
    authorJwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\""
                    + AUTHOR
                    + "\",\"email\":\"crossauthor@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    readerJwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\""
                    + READER
                    + "\",\"email\":\"crossreader@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    slug =
        givenAuth(authorJwt)
            .body(
                "{\"article\":{\"title\":\"Cross Service Article\","
                    + "\"description\":\"Data integrity test\","
                    + "\"body\":\"Multi-service data\","
                    + "\"tagList\":[\"cross-service\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(201)
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(1)
  void readerFollowsAuthor() {
    givenAuth(readerJwt)
        .when()
        .post("/profiles/" + AUTHOR + "/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(2)
  void readerFavoritesArticle() {
    givenAuth(readerJwt)
        .when()
        .post("/articles/" + slug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(3)
  void readerCommentsOnArticle() {
    givenAuth(readerJwt)
        .body("{\"comment\":{\"body\":\"Cross-service comment\"}}")
        .when()
        .post("/articles/" + slug + "/comments")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(4)
  void getArticleShowsAggregatedData() {
    givenAuth(readerJwt)
        .when()
        .get("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.favoritesCount", equalTo(1))
        .body("article.favorited", equalTo(true))
        .body("article.author.username", equalTo(AUTHOR))
        .body("article.author.following", equalTo(true));
  }

  @Test
  @Order(5)
  void getCommentsShowsAuthorProfile() {
    givenAuth(readerJwt)
        .when()
        .get("/articles/" + slug + "/comments")
        .then()
        .statusCode(200)
        .body("comments.size()", equalTo(1))
        .body("comments[0].body", equalTo("Cross-service comment"))
        .body("comments[0].author.username", equalTo(READER));
  }
}
