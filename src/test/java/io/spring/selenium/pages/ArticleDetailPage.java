package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Article detail page (/article/{slug}) including its comment section. */
public class ArticleDetailPage extends BasePage {

  private static final By ARTICLE_TITLE = By.cssSelector(".banner h1");
  private static final By COMMENT_INPUT =
      By.cssSelector("textarea[placeholder='Write a comment...']");
  private static final By POST_COMMENT = By.xpath("//button[normalize-space(.)='Post Comment']");
  private static final By COMMENT_CARD = By.cssSelector(".card .card-text");
  private static final By TAG = By.cssSelector(".article-content .tag-list li");

  private final String baseUrl;

  public ArticleDetailPage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public ArticleDetailPage open(String slug) {
    driver.get(baseUrl + "/article/" + slug);
    wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_TITLE));
    return this;
  }

  public ArticleDetailPage waitForTitle(String title) {
    wait.until(ExpectedConditions.textToBePresentInElementLocated(ARTICLE_TITLE, title));
    return this;
  }

  public String title() {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_TITLE)).getText();
  }

  public String body() {
    return driver.findElement(By.cssSelector(".article-content")).getText();
  }

  public String author() {
    return driver.findElement(By.cssSelector(".article-meta .info a.author")).getText();
  }

  public List<String> tags() {
    return driver.findElements(TAG).stream()
        .map(e -> e.getText().trim())
        .collect(Collectors.toList());
  }

  public List<String> comments() {
    return driver.findElements(COMMENT_CARD).stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }

  public ArticleDetailPage waitForCommentForm() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(COMMENT_INPUT));
    return this;
  }

  public ArticleDetailPage addComment(String body) {
    waitForCommentForm();
    driver.findElement(COMMENT_INPUT).sendKeys(body);
    wait.until(ExpectedConditions.elementToBeClickable(POST_COMMENT)).click();
    wait.until(d -> comments().contains(body));
    return this;
  }

  public ArticleDetailPage deleteComment(String body) {
    WebElement card =
        wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(
                    "//div[contains(@class,'card')][.//p[@class='card-text'][normalize-space(.)="
                        + xpathLiteral(body)
                        + "]]")));
    card.findElement(By.cssSelector(".mod-options i")).click();
    wait.until(d -> !comments().contains(body));
    return this;
  }

  public ProfilePage openAuthorProfile() {
    driver.findElement(By.cssSelector(".article-meta .info a.author")).click();
    ProfilePage page = new ProfilePage(driver, baseUrl);
    page.waitForLoaded();
    return page;
  }

  private static String xpathLiteral(String value) {
    if (!value.contains("'")) {
      return "'" + value + "'";
    }
    return "concat('" + value.replace("'", "',\"'\",'") + "')";
  }
}
