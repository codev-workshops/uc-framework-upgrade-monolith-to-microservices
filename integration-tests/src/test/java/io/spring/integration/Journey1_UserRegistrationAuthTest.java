package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Journey 1: User Registration & Auth
 *
 * <ul>
 *   <li>POST /users (register) → assert 201
 *   <li>POST /users/login → assert 200, extract JWT
 *   <li>GET /user (with JWT) → assert profile returned
 *   <li>PUT /user (update bio) → assert updated
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Journey1_UserRegistrationAuthTest extends IntegrationTestBase {

  private static String jwt;
  private static final String USERNAME = "testuser1";
  private static final String EMAIL = "testuser1@example.com";
  private static final String PASSWORD = "password123";

  @Test
  @Order(1)
  void registerUser() {
    jwt =
        givenJson()
            .body(
                "{\"user\":{\"username\":\""
                    + USERNAME
                    + "\",\"email\":\""
                    + EMAIL
                    + "\",\"password\":\""
                    + PASSWORD
                    + "\"}}")
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .body("user.username", equalTo(USERNAME))
            .body("user.email", equalTo(EMAIL))
            .body("user.token", notNullValue())
            .extract()
            .path("user.token");
  }

  @Test
  @Order(2)
  void loginUser() {
    jwt =
        givenJson()
            .body("{\"user\":{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}}")
            .when()
            .post("/users/login")
            .then()
            .statusCode(200)
            .body("user.token", notNullValue())
            .extract()
            .path("user.token");
  }

  @Test
  @Order(3)
  void getCurrentUser() {
    givenAuth(jwt).when().get("/user").then().statusCode(200).body("user.username", equalTo(USERNAME)).body("user.email", equalTo(EMAIL));
  }

  @Test
  @Order(4)
  void updateUser() {
    givenAuth(jwt)
        .body("{\"user\":{\"bio\":\"I like to code\"}}")
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("I like to code"));
  }
}
