package io.spring.integration.contract;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.spring.integration.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceContractTest extends TestBase {

  private static final String USER_SERVICE_URL =
      System.getProperty("user-service.url", "http://localhost:8081");
  private String userId;
  private String token;

  @BeforeAll
  void registerUser() {
    token = registerAndGetToken("contractuser", "contract@test.com", "password123");
    userId =
        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .when()
            .get(GATEWAY_URL + "/user")
            .then()
            .extract()
            .path("user.username");
  }

  @Test
  @Order(1)
  void internalGetUserByUsername_returnsValidUserData() {
    given()
        .when()
        .get(USER_SERVICE_URL + "/internal/users/by-username/contractuser")
        .then()
        .statusCode(200)
        .body("username", equalTo("contractuser"))
        .body("email", equalTo("contract@test.com"))
        .body("id", notNullValue());
  }

  @Test
  @Order(2)
  void internalGetFollowedUsers_returnsList() {
    String id =
        given()
            .when()
            .get(USER_SERVICE_URL + "/internal/users/by-username/contractuser")
            .then()
            .extract()
            .path("id");

    given()
        .when()
        .get(USER_SERVICE_URL + "/internal/users/" + id + "/followed-users")
        .then()
        .statusCode(200);
  }
}
