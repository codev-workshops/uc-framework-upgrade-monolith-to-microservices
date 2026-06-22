package io.spring.integration.contract;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FavoriteServiceContractTest extends TestBase {

  private static final String FAVORITE_SERVICE_URL =
      System.getProperty("favorite-service.url", "http://localhost:8084");

  @Test
  void internalGetFavoriteCounts_returnsValidCounts() {
    given()
        .when()
        .get(FAVORITE_SERVICE_URL + "/internal/favorites/counts?articleIds=nonexistent")
        .then()
        .statusCode(200);
  }

  @Test
  void internalGetFavoriteCount_returnsSingleCount() {
    given()
        .when()
        .get(FAVORITE_SERVICE_URL + "/internal/favorites/article/nonexistent/count")
        .then()
        .statusCode(200);
  }
}
