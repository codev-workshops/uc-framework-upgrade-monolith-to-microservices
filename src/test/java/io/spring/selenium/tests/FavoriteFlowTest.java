package io.spring.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.ProfilePage;
import io.spring.selenium.utils.ApiClient;
import org.testng.annotations.Test;

/** Favorite / unfavorite an article — favorite-service behind the gateway. */
public class FavoriteFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void favoriteThenUnfavoriteUpdatesTheCount() {
    createTest(
        "favoriteThenUnfavoriteUpdatesTheCount",
        "Favoriting bumps the count in the UI and in favorite-service, unfavoriting reverts it");

    ApiClient api = new ApiClient(apiUrl());
    String username = "e2efav" + System.currentTimeMillis();
    String email = username + "@example.com";
    String token = api.register(username, email, "password123");
    String title = "Favorite Target " + System.currentTimeMillis();
    String slug =
        api.createArticle(token, title, "favorite fixture", "Body for the favorite flow.", "e2e");

    new LoginPage(driver, baseUrl()).open().login(email, "password123", username);

    // The article is read from its author's profile list: the global feed only shows the first
    // page of articles, so a freshly created fixture is not guaranteed to appear there.
    ProfilePage articles = new ProfilePage(driver, baseUrl()).openArticles(username);

    int before = articles.favoritesCount(title);
    assertEquals(before, api.favoritesCount(slug), "UI count should match favorite-service");
    assertFalse(articles.isFavorited(title), "article starts unfavorited");

    articles.toggleFavorite(title);
    assertEquals(articles.favoritesCount(title), before + 1, "count increments in the UI");
    assertTrue(articles.isFavorited(title), "heart button becomes active");
    wait(() -> api.favoritesCount(slug) == before + 1, "favorite-service count to increment");
    attachScreenshot("favorited");

    articles.openArticles(username);
    assertTrue(articles.isFavorited(title), "favorite survives a page reload");
    assertEquals(articles.favoritesCount(title), before + 1);

    articles.toggleFavorite(title);
    assertEquals(articles.favoritesCount(title), before, "count reverts in the UI");
    assertFalse(articles.isFavorited(title), "heart button becomes inactive");
    wait(() -> api.favoritesCount(slug) == before, "favorite-service count to revert");

    articles.openArticles(username);
    assertFalse(articles.isFavorited(title), "unfavorite survives a page reload");
    attachScreenshot("unfavorited");
  }

  private void wait(java.util.function.BooleanSupplier condition, String description) {
    long deadline = System.currentTimeMillis() + 10_000;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    throw new AssertionError("timed out waiting for " + description);
  }
}
