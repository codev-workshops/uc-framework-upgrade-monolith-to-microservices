package io.spring.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
class GatewayIntegrationTest {

  static WireMockServer userAuthServer;
  static WireMockServer profileServer;
  static WireMockServer articleServer;
  static WireMockServer favoriteServer;
  static WireMockServer commentServer;

  @LocalServerPort int port;

  @BeforeAll
  static void startStubs() {
    userAuthServer = newServer();
    profileServer = newServer();
    articleServer = newServer();
    favoriteServer = newServer();
    commentServer = newServer();
  }

  static WireMockServer newServer() {
    WireMockServer s = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    s.start();
    return s;
  }

  @AfterAll
  static void stopStubs() {
    userAuthServer.stop();
    profileServer.stop();
    articleServer.stop();
    favoriteServer.stop();
    commentServer.stop();
  }

  @DynamicPropertySource
  static void serviceUrls(DynamicPropertyRegistry registry) {
    registry.add("services.user-auth.url", () -> userAuthServer.baseUrl());
    registry.add("services.profile.url", () -> profileServer.baseUrl());
    registry.add("services.article.url", () -> articleServer.baseUrl());
    registry.add("services.favorite.url", () -> favoriteServer.baseUrl());
    registry.add("services.comment.url", () -> commentServer.baseUrl());
  }

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
    userAuthServer.resetAll();
    profileServer.resetAll();
    articleServer.resetAll();
    favoriteServer.resetAll();
    commentServer.resetAll();
  }

  @Test
  void routes_get_articles_to_article_service() {
    articleServer.stubFor(
        get(urlPathEqualTo("/articles"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"articles\":[],\"articlesCount\":0}")));

    given().when().get("/articles").then().statusCode(200).body("articlesCount", is(0));
  }

  @Test
  void routes_login_to_user_auth_service() {
    userAuthServer.stubFor(
        post(urlEqualTo("/users/login"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"user\":{\"email\":\"a@b.com\",\"username\":\"bob\",\"token\":\"t\"}}")));

    given()
        .contentType(ContentType.JSON)
        .body("{\"user\":{\"email\":\"a@b.com\",\"password\":\"x\"}}")
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.username", equalTo("bob"));
  }

  @Test
  void favorite_resolves_slug_mutates_then_recomposes() {
    articleServer.stubFor(
        get(urlEqualTo("/internal/articles/my-slug"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":\"a1\",\"slug\":\"my-slug\",\"authorId\":\"u1\"}")));
    favoriteServer.stubFor(
        post(urlEqualTo("/articles/a1/favorite"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"articleId\":\"a1\",\"favorited\":true,\"favoritesCount\":1}")));
    articleServer.stubFor(
        get(urlEqualTo("/internal/articles/a1/data"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"article\":{\"slug\":\"my-slug\",\"title\":\"T\",\"favorited\":true,\"favoritesCount\":1,\"author\":{\"username\":\"u1\"}}}")));

    given()
        .header("Authorization", "Token abc")
        .when()
        .post("/articles/my-slug/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", is(true))
        .body("article.favoritesCount", is(1));
  }

  @Test
  void graphql_article_query_composes_from_article_service() {
    articleServer.stubFor(
        get(urlEqualTo("/articles/my-slug"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"article\":{\"slug\":\"my-slug\",\"title\":\"T\",\"description\":\"d\",\"body\":\"b\",\"tagList\":[\"x\"],\"createdAt\":\"2020-01-01T00:00:00Z\",\"updatedAt\":\"2020-01-01T00:00:00Z\",\"favorited\":false,\"favoritesCount\":0,\"author\":{\"username\":\"u1\",\"bio\":\"\",\"image\":\"\",\"following\":false}}}")));

    given()
        .contentType(ContentType.JSON)
        .body("{\"query\":\"{ article(slug:\\\"my-slug\\\"){ slug title author { username } } }\"}")
        .when()
        .post("/graphql")
        .then()
        .statusCode(200)
        .body("data.article.slug", equalTo("my-slug"))
        .body("data.article.author.username", equalTo("u1"));
  }

  @Test
  void graphql_tags_query_delegates_to_article_service() {
    articleServer.stubFor(
        get(urlEqualTo("/tags"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"tags\":[\"java\",\"spring\"]}")));

    given()
        .contentType(ContentType.JSON)
        .body("{\"query\":\"{ tags }\"}")
        .when()
        .post("/graphql")
        .then()
        .statusCode(200)
        .body("data.tags[0]", equalTo("java"));
  }

  @Test
  void delete_comment_routes_to_comment_service() {
    commentServer.stubFor(
        delete(urlEqualTo("/articles/my-slug/comments/c1"))
            .willReturn(aResponse().withStatus(204)));

    given()
        .header("Authorization", "Token abc")
        .when()
        .delete("/articles/my-slug/comments/c1")
        .then()
        .statusCode(204);
  }
}
