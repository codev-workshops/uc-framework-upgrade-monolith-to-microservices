package io.spring.integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TagsApiIntegrationTest extends TestBase {

  @Test
  void getTags_returnsTagList() {
    given()
        .contentType("application/json")
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags", notNullValue());
  }
}
