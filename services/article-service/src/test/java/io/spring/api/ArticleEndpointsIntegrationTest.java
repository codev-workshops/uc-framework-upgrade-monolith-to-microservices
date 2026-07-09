package io.spring.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.spring.TestJwt;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ArticleEndpointsIntegrationTest {

  private static WireMockServer wireMock;

  @LocalServerPort private int port;

  @BeforeAll
  static void startWireMock() {
    wireMock = new WireMockServer(0);
    wireMock.start();
    configureFor("localhost", wireMock.port());
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  @DynamicPropertySource
  static void upstreamUrls(DynamicPropertyRegistry registry) {
    registry.add("services.user-auth.url", () -> "http://localhost:" + wireMock.port());
    registry.add("services.favorite.url", () -> "http://localhost:" + wireMock.port());
    registry.add("services.profile.url", () -> "http://localhost:" + wireMock.port());
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    wireMock.resetAll();
    stubUpstreams();
  }

  private void stubUpstreams() {
    String authors =
        "[{\"id\":\"user-1\",\"username\":\"jake\",\"bio\":\"bio1\",\"image\":\"img1\"},"
            + "{\"id\":\"user-2\",\"username\":\"john\",\"bio\":\"\",\"image\":\"\"},"
            + "{\"id\":\"user-3\",\"username\":\"jane\",\"bio\":\"\",\"image\":\"\"},"
            + "{\"id\":\"author-x\",\"username\":\"authorx\",\"bio\":\"\",\"image\":\"\"}]";
    stubFor(post(urlPathEqualTo("/internal/users/batch")).willReturn(json(authors)));
    stubFor(
        get(urlPathMatching("/internal/users/by-username/jake"))
            .willReturn(
                json(
                    "{\"id\":\"user-1\",\"username\":\"jake\",\"bio\":\"bio1\",\"image\":\"img1\"}")));
    stubFor(
        get(urlPathMatching("/internal/users/by-username/ghost"))
            .willReturn(aResponse().withStatus(404)));
    stubFor(
        post(urlPathEqualTo("/internal/favorites/counts"))
            .willReturn(json("[{\"id\":\"article-1\",\"count\":3}]")));
    stubFor(
        post(urlPathEqualTo("/internal/favorites/status"))
            .withQueryParam("userId", matching(".*"))
            .willReturn(json("[\"article-1\"]")));
    stubFor(
        get(urlPathEqualTo("/internal/favorites/by-user"))
            .withQueryParam("userId", matching(".*"))
            .willReturn(json("[\"article-1\",\"article-3\"]")));
    stubFor(
        post(urlPathEqualTo("/internal/follows/among"))
            .withQueryParam("userId", matching(".*"))
            .willReturn(json("[\"user-1\"]")));
    stubFor(
        get(urlPathEqualTo("/internal/follows/followed"))
            .withQueryParam("userId", matching(".*"))
            .willReturn(json("[\"user-1\"]")));
  }

  private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(
      String body) {
    return aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody(body);
  }

  @Test
  void getTags_isPublic() {
    given().when().get("/tags").then().statusCode(200).body("tags", hasItem("java"));
  }

  @Test
  void listArticles_public_composesAuthorAndCount() {
    given()
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", greaterThanOrEqualTo(5))
        .body(
            "articles.find { it.slug == 'getting-started-with-spring-boot' }.author.username",
            equalTo("jake"))
        .body(
            "articles.find { it.slug == 'getting-started-with-spring-boot' }.favoritesCount",
            equalTo(3));
  }

  @Test
  void getArticleBySlug_composesAuthor() {
    given()
        .when()
        .get("/articles/getting-started-with-spring-boot")
        .then()
        .statusCode(200)
        .body("article.slug", equalTo("getting-started-with-spring-boot"))
        .body("article.author.username", equalTo("jake"))
        .body("article.favoritesCount", equalTo(3));
  }

  @Test
  void getArticleBySlug_missing_404() {
    given().when().get("/articles/does-not-exist").then().statusCode(404);
  }

  @Test
  void listArticles_authenticated_setsFavoritedAndFollowing() {
    given()
        .header("Authorization", "Bearer " + TestJwt.tokenFor("user-9"))
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body(
            "articles.find { it.slug == 'getting-started-with-spring-boot' }.favorited",
            equalTo(true))
        .body(
            "articles.find { it.slug == 'getting-started-with-spring-boot' }.author.following",
            equalTo(true));
  }

  @Test
  void listArticles_filterByUnknownAuthor_returnsEmpty() {
    given()
        .queryParam("author", "ghost")
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }

  @Test
  void feed_requiresAuth() {
    given().when().get("/articles/feed").then().statusCode(401);
  }

  @Test
  void feed_returnsArticlesOfFollowedAuthors() {
    given()
        .header("Authorization", "Bearer " + TestJwt.tokenFor("user-9"))
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articles.author.username", hasItem("jake"));
  }

  @Test
  void createUpdateDelete_flow_enforcesAuthorship() {
    String author = "author-x";
    String token = TestJwt.tokenFor(author);

    Map<String, Object> article = new HashMap<>();
    article.put("title", "My Brand New Article");
    article.put("description", "desc");
    article.put("body", "body");
    article.put("tagList", java.util.Arrays.asList("java", "newtag"));
    Map<String, Object> payload = new HashMap<>();
    payload.put("article", article);

    String slug =
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .body("article.title", equalTo("My Brand New Article"))
            .body("article.author.username", equalTo("authorx"))
            .extract()
            .path("article.slug");

    // non-author cannot update -> 403
    Map<String, Object> update = new HashMap<>();
    Map<String, Object> updateArticle = new HashMap<>();
    updateArticle.put("body", "hacked");
    update.put("article", updateArticle);
    given()
        .header("Authorization", "Bearer " + TestJwt.tokenFor("someone-else"))
        .contentType(ContentType.JSON)
        .body(update)
        .when()
        .put("/articles/" + slug)
        .then()
        .statusCode(403);

    // author can update
    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body(update)
        .when()
        .put("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.body", equalTo("hacked"));

    // author can delete
    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .delete("/articles/" + slug)
        .then()
        .statusCode(204);
  }

  @Test
  void internalResolveBySlug_returnsIdAndAuthor() {
    given()
        .when()
        .get("/internal/articles/getting-started-with-spring-boot")
        .then()
        .statusCode(200)
        .body("id", equalTo("article-1"))
        .body("slug", equalTo("getting-started-with-spring-boot"))
        .body("authorId", equalTo("user-1"));
  }

  @Test
  void internalRecomposeById_returnsComposedArticle() {
    given()
        .header("Authorization", "Bearer " + TestJwt.tokenFor("user-9"))
        .when()
        .get("/internal/articles/article-1/data")
        .then()
        .statusCode(200)
        .body("article.slug", equalTo("getting-started-with-spring-boot"))
        .body("article.author.username", equalTo("jake"))
        .body("article.favorited", equalTo(true));
  }
}
