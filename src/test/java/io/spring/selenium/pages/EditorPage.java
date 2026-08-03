package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Article editor (/editor/new). */
public class EditorPage extends BasePage {

  private static final By TITLE = By.cssSelector("input[placeholder='Article Title']");
  private static final By DESCRIPTION =
      By.cssSelector("input[placeholder=\"What's this article about?\"]");
  private static final By BODY =
      By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
  private static final By TAGS = By.cssSelector("input[placeholder='Enter tags']");
  private static final By PUBLISH = By.xpath("//button[normalize-space(.)='Publish Article']");

  private final String baseUrl;

  public EditorPage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public EditorPage open() {
    driver.get(baseUrl + "/editor/new");
    wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE));
    return this;
  }

  public EditorPage fill(String title, String description, String body, String... tags) {
    driver.findElement(TITLE).sendKeys(title);
    driver.findElement(DESCRIPTION).sendKeys(description);
    driver.findElement(BODY).sendKeys(body);
    WebElement tagInput = driver.findElement(TAGS);
    for (String tag : tags) {
      tagInput.sendKeys(tag);
      tagInput.sendKeys(Keys.ENTER);
    }
    return this;
  }

  /** Publish and wait for the redirect back to the home feed. */
  public HomePage publish() {
    wait.until(ExpectedConditions.elementToBeClickable(PUBLISH)).click();
    wait.until(d -> d.getCurrentUrl().replaceAll("/$", "").endsWith(baseUrl.replaceAll("/$", "")));
    HomePage home = new HomePage(driver, baseUrl);
    home.waitForArticleList();
    return home;
  }
}
