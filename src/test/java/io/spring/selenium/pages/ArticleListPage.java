package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Shared behaviour of every page that renders the Conduit article-preview list (home feed, tag
 * feed, profile pages): reading titles, tags, favorite counts and toggling the heart button.
 */
public abstract class ArticleListPage extends BasePage {

  protected static final By ARTICLE_PREVIEW = By.cssSelector(".article-preview");
  private static final By FAVORITE_BUTTON = By.cssSelector(".pull-xs-right button");

  protected ArticleListPage(WebDriver driver) {
    super(driver);
  }

  /** The list is rendered asynchronously by SWR, so wait for previews or for the empty state. */
  public void waitForArticleList() {
    wait.until(
        d ->
            !d.findElements(ARTICLE_PREVIEW).isEmpty()
                || !d.findElements(By.xpath("//*[contains(text(),'No articles are here')]"))
                    .isEmpty());
  }

  public List<String> articleTitles() {
    return driver.findElements(By.cssSelector(".article-preview h1")).stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }

  public boolean hasArticle(String title) {
    return articleTitles().contains(title);
  }

  /** Tags shown on each article preview in the current list. */
  public List<List<String>> previewTagLists() {
    return driver.findElements(ARTICLE_PREVIEW).stream()
        .map(
            preview ->
                preview.findElements(By.cssSelector(".tag-list li")).stream()
                    .map(e -> e.getText().trim())
                    .collect(Collectors.toList()))
        .collect(Collectors.toList());
  }

  public boolean showsEmptyFeed() {
    return !driver.findElements(By.xpath("//*[contains(text(),'No articles are here')]")).isEmpty();
  }

  protected WebElement previewFor(String title) {
    return wait.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath(
                "//div[contains(@class,'article-preview')][.//h1[normalize-space(.)="
                    + xpathLiteral(title)
                    + "]]")));
  }

  public int favoritesCount(String title) {
    String text = previewFor(title).findElement(FAVORITE_BUTTON).getText();
    return Integer.parseInt(text.trim().isEmpty() ? "0" : text.trim());
  }

  public boolean isFavorited(String title) {
    return previewFor(title)
        .findElement(FAVORITE_BUTTON)
        .getAttribute("class")
        .contains("btn-primary");
  }

  /** Click the heart button of the given article and wait for the count to change. */
  public void toggleFavorite(String title) {
    int before = favoritesCount(title);
    previewFor(title).findElement(FAVORITE_BUTTON).click();
    wait.until(d -> favoritesCount(title) != before);
  }

  protected static String xpathLiteral(String value) {
    if (!value.contains("'")) {
      return "'" + value + "'";
    }
    return "concat('" + value.replace("'", "',\"'\",'") + "')";
  }
}
