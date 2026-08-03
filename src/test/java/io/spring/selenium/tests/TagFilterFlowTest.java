package io.spring.selenium.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.utils.ApiClient;
import java.util.List;
import org.testng.annotations.Test;

/** Filtering the article list by tag — tag-service + article-service behind the gateway. */
public class TagFilterFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void filterArticlesByPopularTag() {
    createTest(
        "filterArticlesByPopularTag",
        "Clicking a popular tag filters the feed to articles carrying that tag");

    ApiClient api = new ApiClient(apiUrl());
    String tags = api.getTags();

    HomePage home = new HomePage(driver, baseUrl()).open();
    List<String> sidebarTags = home.sidebarTags();
    assertFalse(sidebarTags.isEmpty(), "the popular tags sidebar should be populated");
    for (String tag : sidebarTags) {
      assertTrue(tags.contains(tag), "sidebar tag should come from tag-service: " + tag);
    }

    String tag = sidebarTags.contains("java") ? "java" : sidebarTags.get(0);
    home.clickSidebarTag(tag);

    assertTrue(driver.getCurrentUrl().contains("tag=" + tag));
    List<String> titles = home.articleTitles();
    assertFalse(titles.isEmpty(), "filtering by " + tag + " should return at least one article");
    for (List<String> previewTags : home.previewTagLists()) {
      assertTrue(
          previewTags.contains(tag),
          "every listed article should carry the tag " + tag + " but had " + previewTags);
    }
    test.info("Articles tagged '" + tag + "': " + titles);
    attachScreenshot("tagFilter");

    String filtered = api.send("GET", "/articles?tag=" + tag, null, null);
    for (String title : titles) {
      assertTrue(filtered.contains(title), "UI result should match GET /articles?tag=" + tag);
    }
  }
}
