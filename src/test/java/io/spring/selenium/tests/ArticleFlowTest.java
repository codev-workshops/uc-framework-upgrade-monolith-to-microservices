package io.spring.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.ArticleDetailPage;
import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.utils.ApiClient;
import java.util.List;
import org.testng.annotations.Test;

/** Article creation and the global article list, served by article-service via the gateway. */
public class ArticleFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void createArticleAndSeeItInTheGlobalFeed() {
    createTest(
        "createArticleAndSeeItInTheGlobalFeed",
        "Publish an article through the editor and verify it in the feed and on its detail page");

    String title = "E2E Article " + System.currentTimeMillis();
    new LoginPage(driver, baseUrl()).open().login("john@example.com", "password123", "johndoe");

    HomePage home =
        new EditorPage(driver, baseUrl())
            .open()
            .fill(title, "written by the e2e suite", "Body of the end-to-end article.", "e2e")
            .publish();

    assertTrue(home.hasArticle(title), "the new article should appear in the global feed");
    test.info("Article visible in global feed: " + title);

    ArticleDetailPage detail = home.openArticle(title);
    assertEquals(detail.title(), title);
    assertEquals(detail.author(), "johndoe");
    assertTrue(detail.body().contains("Body of the end-to-end article."));
    assertTrue(detail.tags().contains("e2e"), "tag-service should return the article's tag");
    attachScreenshot("createArticle");
  }

  @Test(groups = {"e2e"})
  public void globalFeedListsSeededArticles() {
    createTest(
        "globalFeedListsSeededArticles",
        "The anonymous global feed matches what the gateway returns from article-service");

    HomePage home = new HomePage(driver, baseUrl()).open();
    List<String> titles = home.articleTitles();

    assertTrue(titles.size() >= 5, "expected at least the 5 seeded articles, got " + titles.size());
    String articles = new ApiClient(apiUrl()).send("GET", "/articles", null, null);
    for (String title : titles) {
      assertTrue(
          articles.contains(title),
          "article shown in the UI should be present in GET /articles: " + title);
    }
    attachScreenshot("globalFeed");
  }
}
