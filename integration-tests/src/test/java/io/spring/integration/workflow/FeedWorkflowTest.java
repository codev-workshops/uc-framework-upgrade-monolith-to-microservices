package io.spring.integration.workflow;

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
public class FeedWorkflowTest extends TestBase {

  private String tokenA;
  private String tokenB;

  @Test
  @Order(1)
  void registerUsers() {
    tokenA = registerAndGetToken("feeduserA", "feeduserA@test.com", "password123");
    tokenB = registerAndGetToken("feeduserB", "feeduserB@test.com", "password123");
  }

  @Test
  @Order(2)
  void userAFollowsUserB() {
    authenticatedRequest(tokenA)
        .when()
        .post("/profiles/feeduserB/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(3)
  void userBCreatesArticle() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Feed Test Article by B");
    article.put("description", "Testing feed");
    article.put("body", "Body content");
    article.put("tagList", List.of("feed"));
    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    authenticatedRequest(tokenB).body(body).when().post("/articles").then().statusCode(200);
  }

  @Test
  @Order(4)
  void userAFeedContainsUserBArticle() {
    authenticatedRequest(tokenA)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articles.size()", greaterThanOrEqualTo(1))
        .body("articles[0].author.username", equalTo("feeduserB"));
  }
}
