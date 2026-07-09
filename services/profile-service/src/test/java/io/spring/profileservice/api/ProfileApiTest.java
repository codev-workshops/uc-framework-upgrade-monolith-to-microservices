package io.spring.profileservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.profileservice.JacksonCustomizations;
import io.spring.profileservice.application.ProfileQueryService;
import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.core.follow.FollowRelation;
import io.spring.profileservice.core.follow.FollowRepository;
import io.spring.profileservice.infrastructure.client.AuthorRef;
import io.spring.profileservice.infrastructure.client.UserAuthClient;
import io.spring.profileservice.security.WebSecurityConfig;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private FollowRepository followRepository;
  @MockBean private UserAuthClient userAuthClient;

  private AuthorRef target;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    target = new AuthorRef("user-2", "john", "bio", "image");
  }

  @Test
  public void should_get_profile_anonymous_following_false() throws Exception {
    when(profileQueryService.findByUsername(eq("john"), eq(null)))
        .thenReturn(Optional.of(new ProfileData("user-2", "john", "bio", "image", false)));

    given()
        .when()
        .get("/profiles/{username}", "john")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.username", equalTo("john"))
        .body("profile.following", equalTo(false));
  }

  @Test
  public void should_get_profile_with_following_true_when_authenticated() throws Exception {
    when(profileQueryService.findByUsername(eq("john"), eq(currentUserId)))
        .thenReturn(Optional.of(new ProfileData("user-2", "john", "bio", "image", true)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/profiles/{username}", "john")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  @Test
  public void should_return_404_when_profile_not_found() throws Exception {
    when(profileQueryService.findByUsername(eq("ghost"), eq(null))).thenReturn(Optional.empty());

    given().when().get("/profiles/{username}", "ghost").then().statusCode(404);
  }

  @Test
  public void should_follow_success() throws Exception {
    when(userAuthClient.findByUsername(eq("john"))).thenReturn(Optional.of(target));
    when(profileQueryService.toProfileData(eq(target), eq(currentUserId)))
        .thenReturn(new ProfileData("user-2", "john", "bio", "image", true));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", "john")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));

    verify(followRepository).saveRelation(new FollowRelation(currentUserId, "user-2"));
  }

  @Test
  public void should_reject_follow_when_anonymous() throws Exception {
    given().when().post("/profiles/{username}/follow", "john").then().statusCode(401);
    verify(followRepository, never()).saveRelation(any());
  }

  @Test
  public void should_unfollow_success() throws Exception {
    when(userAuthClient.findByUsername(eq("john"))).thenReturn(Optional.of(target));
    when(followRepository.findRelation(eq(currentUserId), eq("user-2")))
        .thenReturn(Optional.of(new FollowRelation(currentUserId, "user-2")));
    when(profileQueryService.toProfileData(eq(target), eq(currentUserId)))
        .thenReturn(new ProfileData("user-2", "john", "bio", "image", false));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", "john")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));

    verify(followRepository).removeRelation(new FollowRelation(currentUserId, "user-2"));
  }

  @Test
  public void should_return_404_when_follow_target_missing() throws Exception {
    when(userAuthClient.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", "ghost")
        .then()
        .statusCode(404);
  }
}
