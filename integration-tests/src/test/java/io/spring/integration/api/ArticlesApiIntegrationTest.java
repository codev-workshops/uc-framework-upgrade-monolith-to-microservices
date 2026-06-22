package io.spring.integration.api;

import static io.restassured.RestAssured.given;
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
public class ArticlesApiIntegrationTest extends TestBase {

  private String token;

  @BeforeAll
  void setupUser() {
    token = registerAndGetToken("listtest", "listtest@test.com", "password123");

    for (int i = 0; i < 3; i++) {
      Map<String, Object> article = new HashMap<>();
      article.put("title", "List Article " + i);
      article.put("description", "Description " + i);
      article.put("body", "Body " + i);
      article.put("tagList", List.of("listtest"));
      Map<String, Object> body = new HashMap<>();
      body.put("article", article);

      authenticatedRequest(token).body(body).when().post("/articles").then().statusCode(200);
    }
  }

  @Test
  @Order(1)
  void listArticles_returnsArticles() {
    given()
        .contentType("application/json")
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles", notNullValue())
        .body("articlesCount", greaterThanOrEqualTo(3));
  }

  @Test
  @Order(2)
  void listArticles_filterByTag() {
    given()
        .contentType("application/json")
        .queryParam("tag", "listtest")
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.size()", greaterThanOrEqualTo(3));
  }

  @Test
  @Order(3)
  void listArticles_filterByAuthor() {
    given()
        .contentType("application/json")
        .queryParam("author", "listtest")
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.size()", greaterThanOrEqualTo(3));
  }

  @Test
  @Order(4)
  void getFeed_requiresAuthentication() {
    given().contentType("application/json").when().get("/articles/feed").then().statusCode(401);
  }

  @Test
  @Order(5)
  void getFeed_returnsEmptyForNewUser() {
    String newToken = registerAndGetToken("feedtest", "feedtest@test.com", "password123");
    authenticatedRequest(newToken)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }
}
