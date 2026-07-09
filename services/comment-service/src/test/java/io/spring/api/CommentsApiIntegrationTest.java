package io.spring.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentsApiIntegrationTest {

  private static final String SECRET =
      "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA";

  private static WireMockServer wireMock;

  @Autowired private MockMvc mvc;

  @BeforeAll
  static void startWireMock() {
    wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMock.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  @DynamicPropertySource
  static void serviceUrls(DynamicPropertyRegistry registry) {
    registry.add("services.article.url", () -> wireMock.baseUrl());
    registry.add("services.user-auth.url", () -> wireMock.baseUrl());
    registry.add("services.profile.url", () -> wireMock.baseUrl());
  }

  private static String token(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .signWith(new SecretKeySpec(SECRET.getBytes(), SignatureAlgorithm.HS512.getJcaName()))
        .compact();
  }

  @BeforeEach
  void resetStubs() {
    wireMock.resetAll();
    // slug -> article-1 (authorId user-1)
    wireMock.stubFor(
        WireMock.get(urlPathEqualTo("/internal/articles/test-slug"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"article-1\",\"slug\":\"test-slug\",\"authorId\":\"user-1\"}")));
    // author projection for any user id
    wireMock.stubFor(
        WireMock.get(urlPathMatching("/internal/users/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"someone\",\"username\":\"someone\",\"bio\":\"b\",\"image\":\"i\"}")));
    // by default nobody is followed
    wireMock.stubFor(
        WireMock.post(urlPathEqualTo("/internal/follows/among"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));
  }

  @Test
  void should_return_404_when_article_missing() throws Exception {
    wireMock.stubFor(
        WireMock.get(urlPathEqualTo("/internal/articles/missing"))
            .willReturn(aResponse().withStatus(404)));
    mvc.perform(get("/articles/missing/comments")).andExpect(status().isNotFound());
  }

  @Test
  void should_list_comments_of_article() throws Exception {
    mvc.perform(get("/articles/test-slug/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments.length()", greaterThanOrEqualTo(2)))
        .andExpect(jsonPath("$.comments[*].id", hasItem("comment-1")))
        .andExpect(jsonPath("$.comments[0].author.username").exists())
        .andExpect(jsonPath("$.comments[0].author.following", equalTo(false)));
  }

  @Test
  void should_enrich_following_flag_when_current_user_follows_author() throws Exception {
    // author of comment-1 is user-2; report user-2 as followed
    wireMock.stubFor(
        WireMock.post(urlPathEqualTo("/internal/follows/among"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"user-2\"]")));
    // return author id matching the stored user id so the followed set matches
    wireMock.stubFor(
        WireMock.get(urlPathEqualTo("/internal/users/user-2"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"user-2\",\"username\":\"user-2\",\"bio\":\"b\",\"image\":\"i\"}")));

    mvc.perform(
            get("/articles/test-slug/comments")
                .header("Authorization", "Bearer " + token("user-1")))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.comments[?(@.id=='comment-1')].author.following", hasItem(equalTo(true))));
  }

  @Test
  void should_require_auth_to_create_comment() throws Exception {
    mvc.perform(
            post("/articles/test-slug/comments")
                .contentType("application/json")
                .content("{\"comment\":{\"body\":\"hi\"}}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_return_422_when_body_empty() throws Exception {
    mvc.perform(
            post("/articles/test-slug/comments")
                .header("Authorization", "Bearer " + token("user-1"))
                .contentType("application/json")
                .content("{\"comment\":{\"body\":\"\"}}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.body[0]", equalTo("can't be empty")));
  }

  @Test
  void should_create_comment() throws Exception {
    mvc.perform(
            post("/articles/test-slug/comments")
                .header("Authorization", "Bearer " + token("user-1"))
                .contentType("application/json")
                .content("{\"comment\":{\"body\":\"a new comment\"}}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.body", equalTo("a new comment")))
        .andExpect(jsonPath("$.comment.id").exists())
        .andExpect(jsonPath("$.comment.author").exists());
  }

  @Test
  void should_delete_comment_when_article_author() throws Exception {
    // create as user-2, delete as user-1 (article author) -> authorized
    String id = createComment("user-2", "to delete");
    mvc.perform(
            delete("/articles/test-slug/comments/" + id)
                .header("Authorization", "Bearer " + token("user-1")))
        .andExpect(status().isNoContent());
  }

  @Test
  void should_delete_own_comment() throws Exception {
    String id = createComment("user-2", "mine");
    mvc.perform(
            delete("/articles/test-slug/comments/" + id)
                .header("Authorization", "Bearer " + token("user-2")))
        .andExpect(status().isNoContent());
  }

  @Test
  void should_return_403_when_not_author_of_article_or_comment() throws Exception {
    String id = createComment("user-2", "protected");
    mvc.perform(
            delete("/articles/test-slug/comments/" + id)
                .header("Authorization", "Bearer " + token("user-99")))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return_404_when_deleting_missing_comment() throws Exception {
    mvc.perform(
            delete("/articles/test-slug/comments/does-not-exist")
                .header("Authorization", "Bearer " + token("user-1")))
        .andExpect(status().isNotFound());
  }

  private String createComment(String userId, String body) throws Exception {
    String response =
        mvc.perform(
                post("/articles/test-slug/comments")
                    .header("Authorization", "Bearer " + token(userId))
                    .contentType("application/json")
                    .content("{\"comment\":{\"body\":\"" + body + "\"}}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return com.jayway.jsonpath.JsonPath.read(response, "$.comment.id");
  }
}
