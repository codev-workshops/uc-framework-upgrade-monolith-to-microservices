package io.spring.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

/** Base class for integration tests that configures RestAssured to point at the API Gateway. */
public abstract class IntegrationTestBase {

  protected static final String GATEWAY_URL =
      System.getProperty("gateway.url", "http://localhost:8080");

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = GATEWAY_URL;
    RestAssured.defaultParser = io.restassured.parsing.Parser.JSON;
  }

  protected static io.restassured.specification.RequestSpecification givenJson() {
    return RestAssured.given().contentType(ContentType.JSON);
  }

  protected static io.restassured.specification.RequestSpecification givenAuth(String token) {
    return givenJson().header("Authorization", "Token " + token);
  }
}
