package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The Conduit home page: banner, feed tabs, article list and the popular-tags sidebar. */
public class HomePage extends ArticleListPage {

  private static final By SIDEBAR_TAG = By.cssSelector(".sidebar .tag-list a");
  private static final By FEED_TAB = By.cssSelector(".feed-toggle .nav-link");

  private final String baseUrl;

  public HomePage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public HomePage open() {
    driver.get(baseUrl + "/");
    waitForArticleList();
    return this;
  }

  public HomePage openTag(String tag) {
    driver.get(baseUrl + "/?tag=" + tag);
    waitForArticleList();
    return this;
  }

  public List<String> sidebarTags() {
    wait.until(ExpectedConditions.presenceOfElementLocated(SIDEBAR_TAG));
    return driver.findElements(SIDEBAR_TAG).stream()
        .map(e -> e.getText().trim())
        .collect(Collectors.toList());
  }

  public HomePage clickSidebarTag(String tag) {
    WebElement link =
        wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='sidebar']//a[normalize-space(.)='" + tag + "']")));
    link.click();
    wait.until(ExpectedConditions.urlContains("tag=" + tag));
    waitForArticleList();
    return this;
  }

  public List<String> feedTabs() {
    return driver.findElements(FEED_TAB).stream()
        .map(e -> e.getText().trim())
        .filter(t -> !t.isEmpty())
        .collect(Collectors.toList());
  }

  public HomePage clickFeedTab(String label) {
    WebElement tab =
        wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='feed-toggle']//a[normalize-space(.)='" + label + "']")));
    tab.click();
    waitForArticleList();
    return this;
  }

  public ArticleDetailPage openArticle(String title) {
    previewFor(title).findElement(By.cssSelector("a.preview-link")).click();
    ArticleDetailPage page = new ArticleDetailPage(driver, baseUrl);
    page.waitForTitle(title);
    return page;
  }
}
