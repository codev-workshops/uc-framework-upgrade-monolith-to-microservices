package io.spring.integration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {"spring.datasource.url=jdbc:sqlite:build/it-user-auth.db"})
public class UserAuthIntegrationTest {

  @Autowired private MockMvc mvc;

  @BeforeAll
  public static void cleanDb() {
    new File("build/it-user-auth.db").delete();
  }

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  private Map<String, Object> wrap(Map<String, Object> user) {
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    return body;
  }

  private String loginSeededUser() {
    Map<String, Object> user = new HashMap<>();
    user.put("email", "john@example.com");
    user.put("password", "password123");
    return given()
        .contentType("application/json")
        .body(wrap(user))
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.email", equalTo("john@example.com"))
        .body("user.username", equalTo("johndoe"))
        .extract()
        .path("user.token");
  }

  @Test
  public void should_login_seeded_user_and_issue_token() {
    String token = loginSeededUser();
    org.junit.jupiter.api.Assertions.assertNotNull(token);
  }

  @Test
  public void should_reject_login_with_wrong_password() {
    Map<String, Object> user = new HashMap<>();
    user.put("email", "john@example.com");
    user.put("password", "wrong");
    given()
        .contentType("application/json")
        .body(wrap(user))
        .when()
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("message", equalTo("invalid email or password"));
  }

  @Test
  public void should_get_current_user_with_issued_token() {
    String token = loginSeededUser();
    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo("john@example.com"))
        .body("user.username", equalTo("johndoe"))
        .body("user.token", equalTo(token));
  }

  @Test
  public void should_reject_current_user_without_token() {
    given().when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_update_current_user() {
    String token = loginSeededUser();
    Map<String, Object> user = new HashMap<>();
    user.put("bio", "updated bio from IT");
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(wrap(user))
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("updated bio from IT"));
  }

  @Test
  public void should_register_new_user_and_return_token() {
    long n = System.nanoTime();
    Map<String, Object> user = new HashMap<>();
    user.put("email", "new" + n + "@example.com");
    user.put("username", "newuser" + n);
    user.put("password", "secret123");
    given()
        .contentType("application/json")
        .body(wrap(user))
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("user.email", equalTo("new" + n + "@example.com"))
        .body("user.username", equalTo("newuser" + n))
        .body("user.token", not(equalTo(null)));
  }

  @Test
  public void should_resolve_internal_author_by_id() {
    given()
        .when()
        .get("/internal/users/user-1")
        .then()
        .statusCode(200)
        .body("id", equalTo("user-1"))
        .body("username", equalTo("johndoe"));
  }

  @Test
  public void should_resolve_internal_author_by_username() {
    given()
        .when()
        .get("/internal/users/by-username/janedoe")
        .then()
        .statusCode(200)
        .body("id", equalTo("user-2"))
        .body("username", equalTo("janedoe"));
  }

  @Test
  public void should_batch_resolve_internal_authors() {
    given()
        .contentType("application/json")
        .body(Arrays.asList("user-1", "user-2", "missing"))
        .when()
        .post("/internal/users/batch")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2));
  }
}
