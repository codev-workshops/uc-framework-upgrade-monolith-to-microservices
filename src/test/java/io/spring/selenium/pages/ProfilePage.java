package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** User profile page (/profile/{username}): follow / unfollow plus the author's article list. */
public class ProfilePage extends ArticleListPage {

  private static final By USERNAME = By.cssSelector(".user-info h4");
  private static final By FOLLOW_BUTTON = By.cssSelector("button.action-btn");

  private final String baseUrl;

  public ProfilePage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public ProfilePage open(String username) {
    driver.get(baseUrl + "/profile/" + username);
    waitForLoaded();
    return this;
  }

  /** Open a profile and wait for the author's own article list to render. */
  public ProfilePage openArticles(String username) {
    open(username);
    waitForArticleList();
    return this;
  }

  public ProfilePage waitForLoaded() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
    return this;
  }

  public String username() {
    return driver.findElement(USERNAME).getText();
  }

  public String followButtonText() {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(FOLLOW_BUTTON))
        .getText()
        .trim();
  }

  public boolean isFollowing() {
    return followButtonText().startsWith("Unfollow");
  }

  /**
   * Click follow/unfollow, then reload so the assertion reflects server state, not optimistic UI.
   */
  public ProfilePage toggleFollow() {
    WebElement button = wait.until(ExpectedConditions.elementToBeClickable(FOLLOW_BUTTON));
    boolean wasFollowing = button.getText().trim().startsWith("Unfollow");
    button.click();
    wait.until(
        d -> {
          d.navigate().refresh();
          waitForLoaded();
          return followButtonText().startsWith("Unfollow") != wasFollowing;
        });
    return this;
  }
}
