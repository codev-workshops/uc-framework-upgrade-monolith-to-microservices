package io.spring.integration.workflow;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullUserJourneyTest extends TestBase {

  private String token;
  private String articleSlug;
  private String commentId;

  @Test
  @Order(1)
  void registerUser() {
    token = registerAndGetToken("journeyuser", "journey@test.com", "password123");
  }

  @Test
  @Order(2)
  void createArticle() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Journey Test Article");
    article.put("description", "Full user journey test");
    article.put("body", "This article tests the complete flow");
    article.put("tagList", List.of("journey", "e2e"));
    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    articleSlug =
        authenticatedRequest(token)
            .body(body)
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .body("article.slug", notNullValue())
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(3)
  void addComment() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Journey test comment");
    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    commentId =
        authenticatedRequest(token)
            .body(body)
            .when()
            .post("/articles/" + articleSlug + "/comments")
            .then()
            .statusCode(201)
            .extract()
            .path("comment.id");
  }

  @Test
  @Order(4)
  void favoriteArticle() {
    authenticatedRequest(token)
        .when()
        .post("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(5)
  void verifyArticleShowsFavoriteAndComment() {
    authenticatedRequest(token)
        .when()
        .get("/articles/" + articleSlug)
        .then()
        .statusCode(200)
        .body("article.favoritesCount", equalTo(1))
        .body("article.favorited", equalTo(true));

    given()
        .contentType("application/json")
        .when()
        .get("/articles/" + articleSlug + "/comments")
        .then()
        .statusCode(200)
        .body("comments.size()", greaterThanOrEqualTo(1));
  }
}
