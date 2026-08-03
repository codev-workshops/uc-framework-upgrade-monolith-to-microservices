package io.spring.selenium.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.ArticleDetailPage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.utils.ApiClient;
import org.testng.annotations.Test;

/** Add and delete a comment — comment-service behind the gateway. */
public class CommentFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void addThenDeleteAComment() {
    createTest(
        "addThenDeleteAComment",
        "A comment posted from the article page is stored by comment-service and can be deleted");

    ApiClient api = new ApiClient(apiUrl());
    String username = "e2ecomment" + System.currentTimeMillis();
    String email = username + "@example.com";
    String token = api.register(username, email, "password123");
    String slug =
        api.createArticle(
            token,
            "Comment Target " + System.currentTimeMillis(),
            "comment fixture",
            "Body for the comment flow.",
            "e2e");

    new LoginPage(driver, baseUrl()).open().login(email, "password123", username);

    String body = "End-to-end comment " + System.currentTimeMillis();
    ArticleDetailPage article = new ArticleDetailPage(driver, baseUrl()).open(slug);
    article.addComment(body);

    assertTrue(article.comments().contains(body), "the comment is rendered on the article page");
    assertTrue(api.getComments(slug).contains(body), "comment-service stored the comment");
    attachScreenshot("commentAdded");

    article.open(slug);
    assertTrue(article.comments().contains(body), "the comment survives a reload");

    article.deleteComment(body);
    assertFalse(article.comments().contains(body), "the comment disappears from the page");
    assertFalse(api.getComments(slug).contains(body), "comment-service deleted the comment");
    attachScreenshot("commentDeleted");
  }
}
