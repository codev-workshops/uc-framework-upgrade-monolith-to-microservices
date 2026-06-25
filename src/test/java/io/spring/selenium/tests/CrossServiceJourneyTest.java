package io.spring.selenium.tests;

import static org.testng.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * End-to-end cross-service journey test against the decomposed microservices system through the API
 * gateway. Validates: register/login (user-service), create article with tags (article-service),
 * favorite (favorite-service), comment (comment-service), follow + feed (article-service calling
 * user-service), and cross-service enrichment (author profile, favorite count, follow status).
 */
public class CrossServiceJourneyTest extends BaseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private String apiUrl;
  private String userToken;
  private String secondUserToken;
  private String articleSlug;

  @BeforeClass
  public void setupApiUrl() {
    apiUrl = config.getProperty("api.url", "http://localhost:8080");
  }

  @Test(priority = 1)
  public void testRegisterUser() throws IOException {
    test = extent.createTest("Register User via user-service");
    String body =
        "{\"user\":{\"email\":\"e2euser@test.com\",\"username\":\"e2euser\",\"password\":\"password123\"}}";
    JsonNode response = postJson(apiUrl + "/users", body, null);
    assertNotNull(response.get("user"), "Registration response must contain user");
    userToken = response.get("user").get("token").asText();
    assertNotNull(userToken, "JWT token must be present");
    assertEquals(response.get("user").get("username").asText(), "e2euser");
    test.info("Registered user e2euser, token obtained");
  }

  @Test(priority = 2)
  public void testLoginUser() throws IOException {
    test = extent.createTest("Login User via user-service");
    String body = "{\"user\":{\"email\":\"john@example.com\",\"password\":\"password123\"}}";
    JsonNode response = postJson(apiUrl + "/users/login", body, null);
    assertNotNull(response.get("user"), "Login response must contain user");
    secondUserToken = response.get("user").get("token").asText();
    assertNotNull(secondUserToken, "JWT token must be present for johndoe");
    assertEquals(response.get("user").get("username").asText(), "johndoe");
    test.info("Logged in as johndoe, token obtained");
  }

  @Test(priority = 3)
  public void testGetCurrentUser() throws IOException {
    test = extent.createTest("Get current user via user-service");
    JsonNode response = getJson(apiUrl + "/user", userToken);
    assertNotNull(response.get("user"), "Current user response must contain user");
    assertEquals(response.get("user").get("username").asText(), "e2euser");
    test.info("Current user endpoint works");
  }

  @Test(priority = 4)
  public void testCreateArticleWithTags() throws IOException {
    test = extent.createTest("Create Article with Tags via article-service");
    String body =
        "{\"article\":{\"title\":\"E2E Integration Test Article\","
            + "\"description\":\"Testing cross-service communication\","
            + "\"body\":\"This article validates the decomposed microservices architecture.\","
            + "\"tagList\":[\"e2e\",\"microservices\",\"integration\"]}}";
    JsonNode response = postJson(apiUrl + "/articles", body, userToken);
    assertNotNull(response.get("article"), "Create article response must contain article");
    articleSlug = response.get("article").get("slug").asText();
    assertNotNull(articleSlug, "Article must have a slug");
    assertEquals(
        response.get("article").get("title").asText(), "E2E Integration Test Article");
    assertNotNull(response.get("article").get("author"), "Article must have author profile");
    assertEquals(
        response.get("article").get("author").get("username").asText(),
        "e2euser",
        "Author must be e2euser (cross-service enrichment from user-service)");
    test.info("Article created with slug: " + articleSlug);
  }

  @Test(priority = 5)
  public void testGetArticle() throws IOException {
    test = extent.createTest("Get Article via article-service");
    JsonNode response = getJson(apiUrl + "/articles/" + articleSlug, userToken);
    assertNotNull(response.get("article"), "Get article response must contain article");
    assertEquals(response.get("article").get("slug").asText(), articleSlug);
    assertEquals(
        response.get("article").get("favoritesCount").asInt(),
        0,
        "Initial favorites count should be 0");
    assertFalse(
        response.get("article").get("favorited").asBoolean(),
        "Should not be favorited initially");
    test.info("Article retrieved successfully with cross-service enrichment");
  }

  @Test(priority = 6)
  public void testFavoriteArticle() throws IOException {
    test = extent.createTest("Favorite Article via favorite-service");
    JsonNode response =
        postJson(apiUrl + "/articles/" + articleSlug + "/favorite", "", secondUserToken);
    assertNotNull(response.get("article"), "Favorite response must contain article");
    test.info("Article favorited by johndoe");
  }

  @Test(priority = 7)
  public void testVerifyFavoriteCount() throws IOException {
    test = extent.createTest("Verify favorite count (cross-service: article-service -> favorite-service)");
    JsonNode response = getJson(apiUrl + "/articles/" + articleSlug, secondUserToken);
    assertNotNull(response.get("article"));
    assertEquals(
        response.get("article").get("favoritesCount").asInt(),
        1,
        "Favorites count should be 1 after favoriting (cross-service enrichment from favorite-service)");
    assertTrue(
        response.get("article").get("favorited").asBoolean(),
        "Should be favorited by johndoe (cross-service enrichment from favorite-service)");
    test.info("Favorite count correctly enriched from favorite-service: 1");
  }

  @Test(priority = 8)
  public void testPostComment() throws IOException {
    test = extent.createTest("Post Comment via comment-service");
    String body = "{\"comment\":{\"body\":\"Great cross-service architecture!\"}}";
    JsonNode response =
        postJson(apiUrl + "/articles/" + articleSlug + "/comments", body, secondUserToken);
    assertNotNull(response.get("comment"), "Comment response must contain comment");
    assertEquals(response.get("comment").get("body").asText(), "Great cross-service architecture!");
    assertNotNull(
        response.get("comment").get("author"),
        "Comment must have author (cross-service enrichment from user-service)");
    assertEquals(
        response.get("comment").get("author").get("username").asText(),
        "johndoe",
        "Comment author must be johndoe");
    test.info("Comment posted and author enriched from user-service");
  }

  @Test(priority = 9)
  public void testGetComments() throws IOException {
    test = extent.createTest("Get Comments via comment-service");
    JsonNode response = getJson(apiUrl + "/articles/" + articleSlug + "/comments", secondUserToken);
    assertNotNull(response.get("comments"), "Comments response must contain comments array");
    assertTrue(response.get("comments").size() > 0, "Should have at least one comment");
    test.info("Comments retrieved with author enrichment");
  }

  @Test(priority = 10)
  public void testFollowUser() throws IOException {
    test = extent.createTest("Follow User via user-service");
    JsonNode response =
        postJson(apiUrl + "/profiles/e2euser/follow", "", secondUserToken);
    assertNotNull(response.get("profile"), "Follow response must contain profile");
    assertTrue(
        response.get("profile").get("following").asBoolean(),
        "Should be following e2euser after follow");
    test.info("johndoe now follows e2euser");
  }

  @Test(priority = 11)
  public void testGetFeed() throws IOException {
    test =
        extent.createTest(
            "Get Feed (cross-service: article-service -> user-service for follow graph)");
    JsonNode response = getJson(apiUrl + "/articles/feed", secondUserToken);
    assertNotNull(response.get("articles"), "Feed response must contain articles");
    assertTrue(
        response.get("articlesCount").asInt() > 0,
        "Feed should contain articles from followed users (e2euser)");
    boolean foundArticle = false;
    for (JsonNode article : response.get("articles")) {
      if (article.get("slug").asText().equals(articleSlug)) {
        foundArticle = true;
        assertEquals(
            article.get("author").get("username").asText(),
            "e2euser",
            "Author profile must be enriched from user-service");
        assertTrue(
            article.get("author").get("following").asBoolean(),
            "Author following status must be true (cross-service enrichment)");
        break;
      }
    }
    assertTrue(foundArticle, "Feed must contain e2euser's article");
    test.info("Feed correctly shows articles from followed users with cross-service enrichment");
  }

  @Test(priority = 12)
  public void testGetProfile() throws IOException {
    test = extent.createTest("Get Profile via user-service");
    JsonNode response = getJson(apiUrl + "/profiles/e2euser", secondUserToken);
    assertNotNull(response.get("profile"), "Profile response must contain profile");
    assertEquals(response.get("profile").get("username").asText(), "e2euser");
    assertTrue(
        response.get("profile").get("following").asBoolean(),
        "johndoe should be following e2euser");
    test.info("Profile endpoint works with follow status");
  }

  @Test(priority = 13)
  public void testGetTags() throws IOException {
    test = extent.createTest("Get Tags via article-service");
    JsonNode response = getJson(apiUrl + "/tags", null);
    assertNotNull(response.get("tags"), "Tags response must contain tags");
    assertTrue(response.get("tags").size() > 0, "Should have tags from seed data + test article");
    test.info("Tags endpoint works");
  }

  @Test(priority = 14)
  public void testListArticles() throws IOException {
    test = extent.createTest("List articles with cross-service enrichment");
    JsonNode response = getJson(apiUrl + "/articles", secondUserToken);
    assertNotNull(response.get("articles"), "Articles response must contain articles");
    assertTrue(response.get("articlesCount").asInt() > 0, "Should have articles");
    for (JsonNode article : response.get("articles")) {
      assertNotNull(
          article.get("author"), "Each article must have author profile (cross-service enrichment)");
      assertNotNull(article.get("author").get("username"), "Author must have username");
    }
    test.info("Article list with cross-service profile and favorite enrichment verified");
  }

  @Test(priority = 15)
  public void testUnfavoriteArticle() throws IOException {
    test = extent.createTest("Unfavorite Article via favorite-service");
    deleteRequest(apiUrl + "/articles/" + articleSlug + "/favorite", secondUserToken);
    JsonNode response = getJson(apiUrl + "/articles/" + articleSlug, secondUserToken);
    assertEquals(
        response.get("article").get("favoritesCount").asInt(),
        0,
        "Favorites count should be 0 after unfavoriting");
    assertFalse(
        response.get("article").get("favorited").asBoolean(),
        "Should not be favorited after unfavorite");
    test.info("Unfavorite and cross-service count update verified");
  }

  private JsonNode postJson(String url, String body, String token) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Token " + token);
    }
    if (body != null && !body.isEmpty()) {
      conn.setDoOutput(true);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
    }
    return readResponse(conn);
  }

  private JsonNode getJson(String url, String token) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-Type", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Token " + token);
    }
    return readResponse(conn);
  }

  private void deleteRequest(String url, String token) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty("Content-Type", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Token " + token);
    }
    conn.getResponseCode();
    conn.disconnect();
  }

  private JsonNode readResponse(HttpURLConnection conn) throws IOException {
    int code = conn.getResponseCode();
    String responseBody;
    if (code >= 200 && code < 300) {
      try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
        responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
      }
    } else {
      try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name())) {
        responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
      }
      fail("HTTP " + code + ": " + responseBody);
    }
    conn.disconnect();
    return objectMapper.readTree(responseBody);
  }
}
