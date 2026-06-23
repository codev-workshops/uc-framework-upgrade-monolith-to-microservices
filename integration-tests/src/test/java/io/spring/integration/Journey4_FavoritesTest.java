package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 4: Favorites
 *
 * <ul>
 *   <li>POST /articles/{slug}/favorite → assert favoritesCount = 1, favorited = true
 *   <li>GET /articles/{slug} → verify favoritesCount persisted
 *   <li>DELETE /articles/{slug}/favorite → assert favoritesCount = 0
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey4_FavoritesTest extends IntegrationTestBase {

  private static String jwt;
  private static String slug;

  @BeforeAll
  static void setupUserAndArticle() {
    jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\"favuser\",\"email\":\"favuser@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");

    slug =
        givenAuth(jwt)
            .body(
                "{\"article\":{\"title\":\"Favorite Article\","
                    + "\"description\":\"For fav test\","
                    + "\"body\":\"Content\","
                    + "\"tagList\":[\"fav\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(201)
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(1)
  void favoriteArticle() {
    givenAuth(jwt)
        .when()
        .post("/articles/" + slug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(2)
  void getFavoritedArticle() {
    givenAuth(jwt)
        .when()
        .get("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(3)
  void unfavoriteArticle() {
    givenAuth(jwt)
        .when()
        .delete("/articles/" + slug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(false))
        .body("article.favoritesCount", equalTo(0));
  }
}
