package io.spring.integration.contract;

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
public class ArticleServiceContractTest extends TestBase {

  private static final String ARTICLE_SERVICE_URL =
      System.getProperty("article-service.url", "http://localhost:8082");
  private String token;
  private String articleSlug;

  @BeforeAll
  void setup() {
    token = registerAndGetToken("articlecontract", "articlecontract@test.com", "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Contract Test Article");
    article.put("description", "Testing article service contracts");
    article.put("body", "This is a test article body");
    article.put("tagList", List.of("test", "contract"));
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
  @Order(1)
  void internalGetArticleBySlug_returnsValidData() {
    given()
        .when()
        .get(ARTICLE_SERVICE_URL + "/internal/articles/by-slug/" + articleSlug)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("slug", equalTo(articleSlug));
  }
}
