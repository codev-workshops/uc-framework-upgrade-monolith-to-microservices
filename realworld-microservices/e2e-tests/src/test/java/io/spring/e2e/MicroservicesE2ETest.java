package io.spring.e2e;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroservicesE2ETest {
    private static String token;
    private static String username;
    private static String email;
    private static String password;
    private static String slug;
    private static String commentId;
    private static String secondToken;
    private static String secondUsername;
    private static String secondEmail;
    private static String secondPassword;
    private static String tagName;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = System.getProperty("gateway.url", "http://localhost:8080");
        String unique = UUID.randomUUID().toString().substring(0, 8);
        username = "user" + unique;
        email = username + "@test.com";
        password = "password123";
        secondUsername = "user2" + unique;
        secondEmail = secondUsername + "@test.com";
        secondPassword = "password456";
        tagName = "tag" + unique;
    }

    @Test
    @Order(1)
    void testRegisterUser() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"user\": {\"username\": \"" + username + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\"}}")
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("user.username", equalTo(username))
                .body("user.email", equalTo(email))
                .body("user.token", notNullValue())
                .extract().response();
        token = response.path("user.token");
    }

    @Test
    @Order(2)
    void testLoginUser() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"user\": {\"email\": \"" + email + "\", \"password\": \"" + password + "\"}}")
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("user.token", notNullValue())
                .extract().response();
        token = response.path("user.token");
    }

    @Test
    @Order(3)
    void testGetCurrentUser() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .body("user.username", equalTo(username))
                .body("user.email", equalTo(email));
    }

    @Test
    @Order(4)
    void testUpdateUser() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Token " + token)
                .body("{\"user\": {\"bio\": \"Updated bio for e2e test\"}}")
                .when()
                .put("/user")
                .then()
                .statusCode(200)
                .body("user.bio", equalTo("Updated bio for e2e test"));
    }

    @Test
    @Order(5)
    void testRegisterSecondUser() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"user\": {\"username\": \"" + secondUsername + "\", \"email\": \"" + secondEmail + "\", \"password\": \"" + secondPassword + "\"}}")
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("user.username", equalTo(secondUsername))
                .body("user.token", notNullValue())
                .extract().response();
        secondToken = response.path("user.token");
    }

    @Test
    @Order(6)
    void testFollowUser() {
        given()
                .header("Authorization", "Token " + secondToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/profiles/" + username + "/follow")
                .then()
                .statusCode(200)
                .body("profile.following", equalTo(true));
    }

    @Test
    @Order(7)
    void testGetTags() {
        given()
                .when()
                .get("/tags")
                .then()
                .statusCode(200)
                .body("tags", notNullValue());
    }

    @Test
    @Order(8)
    void testCreateArticleWithTags() {
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Token " + token)
                .body("{\"article\": {\"title\": \"E2E Test Article " + UUID.randomUUID().toString().substring(0, 8) + "\", \"description\": \"E2E test description\", \"body\": \"E2E test body content\", \"tagList\": [\"" + tagName + "\"]}}")
                .when()
                .post("/articles")
                .then()
                .statusCode(200)
                .body("article.title", notNullValue())
                .body("article.slug", notNullValue())
                .extract().response();
        slug = response.path("article.slug");
    }

    @Test
    @Order(9)
    void testGetArticleBySlug() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .get("/articles/" + slug)
                .then()
                .statusCode(200)
                .body("article.slug", equalTo(slug));
    }

    @Test
    @Order(10)
    void testFavoriteArticle() {
        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .when()
                .post("/articles/" + slug + "/favorite")
                .then()
                .statusCode(200)
                .body("article.favorited", equalTo(true));
    }

    @Test
    @Order(11)
    void testUnfavoriteArticle() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/" + slug + "/favorite")
                .then()
                .statusCode(200)
                .body("article.favorited", equalTo(false));
    }

    @Test
    @Order(12)
    void testCreateComment() {
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Token " + token)
                .body("{\"comment\": {\"body\": \"E2E test comment\"}}")
                .when()
                .post("/articles/" + slug + "/comments")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("comment.body", equalTo("E2E test comment"))
                .extract().response();
        commentId = response.path("comment.id").toString();
    }

    @Test
    @Order(13)
    void testListComments() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .get("/articles/" + slug + "/comments")
                .then()
                .statusCode(200)
                .body("comments", notNullValue());
    }

    @Test
    @Order(14)
    void testDeleteComment() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/" + slug + "/comments/" + commentId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test
    @Order(15)
    void testListArticlesByTag() {
        given()
                .when()
                .get("/articles?tag=" + tagName)
                .then()
                .statusCode(200)
                .body("articles", notNullValue());
    }

    @Test
    @Order(16)
    void testGetFeed() {
        given()
                .header("Authorization", "Token " + secondToken)
                .when()
                .get("/articles/feed")
                .then()
                .statusCode(200)
                .body("articles", notNullValue());
    }

    @Test
    @Order(17)
    void testUnfollowUser() {
        given()
                .header("Authorization", "Token " + secondToken)
                .when()
                .delete("/profiles/" + username + "/follow")
                .then()
                .statusCode(200)
                .body("profile.following", equalTo(false));
    }

    @Test
    @Order(18)
    void testDeleteArticle() {
        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/" + slug)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(204)));
    }
}
