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
public class ArticleDeletionWorkflowTest extends TestBase {

  private String token;
  private String articleSlug;

  @Test
  @Order(1)
  void setup() {
    token = registerAndGetToken("deletetest", "deletetest@test.com", "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Article To Delete");
    article.put("description", "Will be deleted");
    article.put("body", "Body");
    article.put("tagList", List.of("delete"));
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
  @Order(2)
  void addCommentAndFavorite() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Comment on article to delete");
    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    authenticatedRequest(token)
        .body(body)
        .when()
        .post("/articles/" + articleSlug + "/comments")
        .then()
        .statusCode(201);

    authenticatedRequest(token)
        .when()
        .post("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(3)
  void deleteArticle() {
    authenticatedRequest(token).when().delete("/articles/" + articleSlug).then().statusCode(204);
  }

  @Test
  @Order(4)
  void getDeletedArticle_returns404() {
    given()
        .contentType("application/json")
        .when()
        .get("/articles/" + articleSlug)
        .then()
        .statusCode(404);
  }
}
