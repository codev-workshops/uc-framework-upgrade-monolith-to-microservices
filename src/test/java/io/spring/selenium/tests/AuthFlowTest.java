package io.spring.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.RegisterPage;
import io.spring.selenium.utils.ApiClient;
import org.testng.annotations.Test;

/** Registration and login against the composed system (gateway -> user-service). */
public class AuthFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void registerNewUser() {
    createTest("registerNewUser", "Register a brand new user through the UI and land logged in");

    String username = "e2euser" + System.currentTimeMillis();
    HomePage home =
        new RegisterPage(driver, baseUrl())
            .open()
            .register(username, username + "@example.com", "password123");

    assertTrue(home.isLoggedInAs(username), "navbar should show the new user's profile link");
    test.info("Registered and authenticated as " + username);
    attachScreenshot("registerNewUser");
  }

  @Test(groups = {"e2e"})
  public void loginExistingUser() {
    createTest("loginExistingUser", "Log in with a seeded user and land on the home feed");

    HomePage home =
        new LoginPage(driver, baseUrl()).open().login("john@example.com", "password123", "johndoe");

    assertTrue(home.isLoggedInAs("johndoe"), "navbar should show johndoe's profile link");
    assertTrue(home.articleTitles().size() > 0, "global feed should list seeded articles");
    attachScreenshot("loginExistingUser");
  }

  @Test(groups = {"e2e"})
  public void loggedInUserMatchesGatewayUserEndpoint() {
    createTest(
        "loggedInUserMatchesGatewayUserEndpoint",
        "The UI session token is accepted by the gateway's /user endpoint");

    ApiClient api = new ApiClient(apiUrl());
    String username = "e2eapi" + System.currentTimeMillis();
    api.register(username, username + "@example.com", "password123");

    HomePage home =
        new LoginPage(driver, baseUrl())
            .open()
            .login(username + "@example.com", "password123", username);
    assertTrue(home.isLoggedInAs(username));

    String token = api.login(username + "@example.com", "password123");
    String currentUser = api.send("GET", "/user", null, token);
    assertTrue(currentUser.contains(username), "GET /user should return the logged in user");
    assertEquals(home.isLoggedOut(), false, "navbar should not show the Sign in link");
  }
}
