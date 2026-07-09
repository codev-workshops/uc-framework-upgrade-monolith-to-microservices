package io.spring.profileservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.profileservice.JacksonCustomizations;
import io.spring.profileservice.infrastructure.mybatis.readservice.FollowsQueryService;
import io.spring.profileservice.security.WebSecurityConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalFollowApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class InternalFollowApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private FollowsQueryService followsQueryService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_report_following_status() throws Exception {
    when(followsQueryService.isUserFollowing(eq("user-1"), eq("user-2"))).thenReturn(true);

    given()
        .when()
        .get("/internal/follows?userId={u}&targetId={t}", "user-1", "user-2")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("following", equalTo(true));
  }

  @Test
  public void should_list_followed_ids() throws Exception {
    when(followsQueryService.followedUsers(eq("user-3")))
        .thenReturn(Arrays.asList("user-1", "user-2"));

    given()
        .when()
        .get("/internal/follows/followed?userId={u}", "user-3")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("$", contains("user-1", "user-2"));
  }

  @Test
  public void should_return_subset_of_followed_authors() throws Exception {
    when(followsQueryService.followingAuthors(eq("user-3"), eq(Arrays.asList("user-1", "user-9"))))
        .thenReturn(new HashSet<>(Collections.singletonList("user-1")));

    given()
        .contentType("application/json")
        .body("[\"user-1\",\"user-9\"]")
        .when()
        .post("/internal/follows/among?userId={u}", "user-3")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("$", containsInAnyOrder("user-1"));
  }

  @Test
  public void should_return_empty_for_empty_among_body() throws Exception {
    given()
        .contentType("application/json")
        .body("[]")
        .when()
        .post("/internal/follows/among?userId={u}", "user-3")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("$", hasSize(0));
  }
}
