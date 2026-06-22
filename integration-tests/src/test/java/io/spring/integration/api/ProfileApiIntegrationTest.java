package io.spring.integration.api;

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
public class ProfileApiIntegrationTest extends TestBase {

  private String tokenA;
  private String tokenB;

  @BeforeAll
  void setupUsers() {
    tokenA = registerAndGetToken("profiletestA", "profileA@test.com", "password123");
    tokenB = registerAndGetToken("profiletestB", "profileB@test.com", "password123");
  }

  @Test
  @Order(1)
  void getProfile_returnsProfileData() {
    given()
        .contentType("application/json")
        .when()
        .get("/profiles/profiletestA")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo("profiletestA"))
        .body("profile.following", equalTo(false));
  }

  @Test
  @Order(2)
  void followUser_setsFollowingTrue() {
    authenticatedRequest(tokenA)
        .when()
        .post("/profiles/profiletestB/follow")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo("profiletestB"))
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(3)
  void getProfile_afterFollow_showsFollowing() {
    authenticatedRequest(tokenA)
        .when()
        .get("/profiles/profiletestB")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  @Order(4)
  void unfollowUser_setsFollowingFalse() {
    authenticatedRequest(tokenA)
        .when()
        .delete("/profiles/profiletestB/follow")
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));
  }
}
