package io.spring.integration.api;

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
public class ArticleApiIntegrationTest extends TestBase {

  private String token;
  private String articleSlug;

  @BeforeAll
  void setupUser() {
    token = registerAndGetToken("articletest", "article@test.com", "password123");
  }

  @Test
  @Order(1)
  void createArticle_returnsArticleData() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Integration Test Article");
    article.put("description", "Testing article creation through gateway");
    article.put("body", "This article tests the full microservices flow");
    article.put("tagList", List.of("integration", "test"));
    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    articleSlug =
        authenticatedRequest(token)
            .body(body)
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .body("article.title", equalTo("Integration Test Article"))
            .body("article.slug", notNullValue())
            .body("article.tagList", hasItems("integration", "test"))
            .extract()
            .path("article.slug");
  }

  @Test
  @Order(2)
  void getArticle_returnsCreatedArticle() {
    authenticatedRequest(token)
        .when()
        .get("/articles/" + articleSlug)
        .then()
        .statusCode(200)
        .body("article.title", equalTo("Integration Test Article"))
        .body("article.author.username", equalTo("articletest"));
  }

  @Test
  @Order(3)
  void updateArticle_updatesTitle() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Updated Integration Test Article");
    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    authenticatedRequest(token)
        .body(body)
        .when()
        .put("/articles/" + articleSlug)
        .then()
        .statusCode(200)
        .body("article.title", equalTo("Updated Integration Test Article"));
  }

  @Test
  @Order(4)
  void deleteArticle_returns204() {
    authenticatedRequest(token).when().delete("/articles/" + articleSlug).then().statusCode(204);
  }
}
