package io.spring.integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersApiIntegrationTest extends TestBase {

  private String token;

  @Test
  @Order(1)
  void register_returnsUserWithToken() {
    Map<String, Object> user = new HashMap<>();
    user.put("username", "testuser1");
    user.put("email", "testuser1@test.com");
    user.put("password", "password123");
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    token =
        given()
            .contentType("application/json")
            .body(body)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .body("user.username", equalTo("testuser1"))
            .body("user.email", equalTo("testuser1@test.com"))
            .body("user.token", notNullValue())
            .extract()
            .path("user.token");
  }

  @Test
  @Order(2)
  void login_returnsUserWithToken() {
    Map<String, Object> user = new HashMap<>();
    user.put("email", "testuser1@test.com");
    user.put("password", "password123");
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.username", equalTo("testuser1"))
        .body("user.token", notNullValue());
  }

  @Test
  @Order(3)
  void register_withDuplicateEmail_returns422() {
    Map<String, Object> user = new HashMap<>();
    user.put("username", "testuser1dup");
    user.put("email", "testuser1@test.com");
    user.put("password", "password123");
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(4)
  void login_withWrongPassword_returns422() {
    Map<String, Object> user = new HashMap<>();
    user.put("email", "testuser1@test.com");
    user.put("password", "wrongpassword");
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/users/login")
        .then()
        .statusCode(422);
  }
}
