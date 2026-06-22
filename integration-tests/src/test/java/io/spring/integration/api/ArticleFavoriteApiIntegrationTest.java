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
public class ArticleFavoriteApiIntegrationTest extends TestBase {

  private String token;
  private String articleSlug;

  @BeforeAll
  void setupArticle() {
    token = registerAndGetToken("favtest", "favtest@test.com", "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Article for Favorite Test");
    article.put("description", "Test favorites");
    article.put("body", "Body");
    article.put("tagList", List.of("favorites"));
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
  void favoriteArticle_returnsFavoritedArticle() {
    authenticatedRequest(token)
        .when()
        .post("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  @Order(2)
  void unfavoriteArticle_returnsUnfavoritedArticle() {
    authenticatedRequest(token)
        .when()
        .delete("/articles/" + articleSlug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(false))
        .body("article.favoritesCount", equalTo(0));
  }
}
