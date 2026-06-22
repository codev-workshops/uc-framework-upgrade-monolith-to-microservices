package io.spring.integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CurrentUserApiIntegrationTest extends TestBase {

  private String token;

  @BeforeAll
  void setupUser() {
    token = registerAndGetToken("currentusertest", "currentuser@test.com", "password123");
  }

  @Test
  @Order(1)
  void getCurrentUser_returnsUserData() {
    authenticatedRequest(token)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.username", equalTo("currentusertest"))
        .body("user.email", equalTo("currentuser@test.com"));
  }

  @Test
  @Order(2)
  void updateCurrentUser_updatesProfile() {
    Map<String, Object> user = new HashMap<>();
    user.put("bio", "Updated bio for test");
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    authenticatedRequest(token)
        .body(body)
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("Updated bio for test"));
  }

  @Test
  @Order(3)
  void getCurrentUser_withoutAuth_returns401() {
    given().contentType("application/json").when().get("/user").then().statusCode(401);
  }
}
