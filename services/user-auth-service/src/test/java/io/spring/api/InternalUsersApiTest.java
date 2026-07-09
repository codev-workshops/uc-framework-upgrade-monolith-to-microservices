package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalUsersApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class InternalUsersApiTest {

  @Autowired private MockMvc mvc;

  @MockBean private UserQueryService userQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;

  private UserData userData;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    userData = new UserData("user-1", "john@example.com", "johndoe", "bio here", "img.png");
  }

  @Test
  public void should_resolve_author_by_id() throws Exception {
    when(userQueryService.findById(eq("user-1"))).thenReturn(Optional.of(userData));

    given()
        .when()
        .get("/internal/users/user-1")
        .then()
        .statusCode(200)
        .body("id", equalTo("user-1"))
        .body("username", equalTo("johndoe"))
        .body("bio", equalTo("bio here"))
        .body("image", equalTo("img.png"));
  }

  @Test
  public void should_return_404_when_author_missing() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Optional.empty());

    given().when().get("/internal/users/nope").then().statusCode(404);
  }

  @Test
  public void should_resolve_author_by_username() throws Exception {
    when(userQueryService.findByUsername(eq("johndoe"))).thenReturn(Optional.of(userData));

    given()
        .when()
        .get("/internal/users/by-username/johndoe")
        .then()
        .statusCode(200)
        .body("id", equalTo("user-1"))
        .body("username", equalTo("johndoe"));
  }

  @Test
  public void should_return_404_when_username_missing() throws Exception {
    when(userQueryService.findByUsername(any())).thenReturn(Optional.empty());

    given().when().get("/internal/users/by-username/ghost").then().statusCode(404);
  }

  @Test
  public void should_batch_resolve_authors_skipping_missing() throws Exception {
    UserData jane = new UserData("user-2", "jane@example.com", "janedoe", "", "");
    when(userQueryService.findById(eq("user-1"))).thenReturn(Optional.of(userData));
    when(userQueryService.findById(eq("user-2"))).thenReturn(Optional.of(jane));
    when(userQueryService.findById(eq("missing"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .body(Arrays.asList("user-1", "missing", "user-2"))
        .when()
        .post("/internal/users/batch")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2))
        .body("[0].id", equalTo("user-1"))
        .body("[1].id", equalTo("user-2"));
  }
}
