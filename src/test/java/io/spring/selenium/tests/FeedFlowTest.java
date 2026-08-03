package io.spring.selenium.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.ProfilePage;
import io.spring.selenium.utils.ApiClient;
import org.testng.annotations.Test;

/**
 * The personalized feed ("Your Feed") — article-service composing the follow graph from
 * profile-service.
 */
public class FeedFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void personalizedFeedFollowsTheSocialGraph() {
    createTest(
        "personalizedFeedFollowsTheSocialGraph",
        "A fresh user's feed is empty until they follow an author, then shows that author's articles");

    ApiClient api = new ApiClient(apiUrl());
    String username = "e2efeed" + System.currentTimeMillis();
    String email = username + "@example.com";
    api.register(username, email, "password123");

    HomePage home = new LoginPage(driver, baseUrl()).open().login(email, "password123", username);
    assertTrue(home.feedTabs().contains("Your Feed"), "logged in users get a personalized tab");

    home.clickFeedTab("Your Feed");
    assertTrue(home.showsEmptyFeed(), "a user who follows nobody has an empty feed");
    test.info("Empty personalized feed confirmed for " + username);

    new ProfilePage(driver, baseUrl()).open("johndoe").toggleFollow();

    HomePage afterFollow = new HomePage(driver, baseUrl()).open().clickFeedTab("Your Feed");
    assertFalse(afterFollow.showsEmptyFeed(), "feed should not be empty after following johndoe");
    assertTrue(
        afterFollow.articleTitles().size() > 0, "personalized feed should list johndoe's articles");
    test.info("Personalized feed articles: " + afterFollow.articleTitles());
    attachScreenshot("personalizedFeed");
  }
}
