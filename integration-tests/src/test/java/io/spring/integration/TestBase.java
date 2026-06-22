package io.spring.integration;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;

public abstract class TestBase {

  protected static final String GATEWAY_URL =
      System.getProperty("gateway.url", "http://localhost:8080");

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = GATEWAY_URL;
  }

  protected String registerAndGetToken(String username, String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("username", username);
    user.put("email", email);
    user.put("password", password);
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    return given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .extract()
        .path("user.token");
  }

  protected String loginAndGetToken(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    return given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .extract()
        .path("user.token");
  }

  protected RequestSpecification authenticatedRequest(String token) {
    return given().contentType("application/json").header("Authorization", "Token " + token);
  }
}
