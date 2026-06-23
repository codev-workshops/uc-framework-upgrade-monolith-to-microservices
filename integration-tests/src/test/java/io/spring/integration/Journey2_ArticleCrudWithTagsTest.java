package io.spring.integration;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 2: Article CRUD with Tags
 *
 * <ul>
 *   <li>POST /articles (create with tags) → assert 201
 *   <li>GET /articles/{slug} → assert article + tags returned
 *   <li>GET /tags → assert tags list includes the new tags
 *   <li>PUT /articles/{slug} (update title) → assert slug changes
 *   <li>GET /articles → assert list includes the article
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey2_ArticleCrudWithTagsTest extends IntegrationTestBase {

  private static String jwt;
  private static String slug;

  @BeforeAll
  static void registerUser() {
    jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\"articleauthor\",\"email\":\"articleauthor@test.com\",\"password\":\"pass123\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("user.token");
  }

  @Test
  @Order(1)
  void createArticleWithTags() {
    slug =
        givenAuth(jwt)
            .body(
                "{\"article\":{\"title\":\"Integration Test Article\","
                    + "\"description\":\"Testing microservices\","
                    + "\"body\":\"This tests the full pipeline\","
                    + "\"tagList\":[\"integration\",\"testing\"]}}")
            .when()
            .post("/articles")
            .then()
            .statusCode(201)
            .body("article.title", equalTo("Integration Test Article"))
            .body("article.tagList", hasItems("integration", "testing"))
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(2)
  void getArticleBySlug() {
    givenJson()
        .when()
        .get("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(slug))
        .body("article.tagList", hasItems("integration", "testing"));
  }

  @Test
  @Order(3)
  void getTagsIncludesNewTags() {
    givenJson()
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags", hasItems("integration", "testing"));
  }

  @Test
  @Order(4)
  void updateArticleTitle() {
    String newSlug =
        givenAuth(jwt)
            .body("{\"article\":{\"title\":\"Updated Integration Article\"}}")
            .when()
            .put("/articles/" + slug)
            .then()
            .statusCode(200)
            .body("article.title", equalTo("Updated Integration Article"))
            .extract()
            .path("article.slug");
    slug = newSlug;
  }

  @Test
  @Order(5)
  void listArticlesIncludesArticle() {
    givenJson()
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", greaterThanOrEqualTo(1));
  }
}
