package io.spring.articleservice;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.articleservice.domain.Article;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class ArticleTest {

  @Test
  void shouldCreateArticleWithGeneratedIdAndSlug() {
    Article article =
        new Article("Test Article Title", "A description", "Article body", Arrays.asList("java", "spring"), "user-1");

    assertNotNull(article.getId());
    assertFalse(article.getId().isEmpty());
    assertEquals("test-article-title", article.getSlug());
    assertEquals("Test Article Title", article.getTitle());
    assertEquals("A description", article.getDescription());
    assertEquals("Article body", article.getBody());
    assertEquals("user-1", article.getUserId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(article.getCreatedAt(), article.getUpdatedAt());
    assertEquals(2, article.getTags().size());
  }

  @Test
  void shouldGenerateSlugFromTitle() {
    assertEquals("hello-world", Article.toSlug("Hello World"));
    assertEquals("a-b-c", Article.toSlug("a & b & c"));
    assertEquals("spring-boot-tutorial", Article.toSlug("Spring Boot Tutorial"));
    assertEquals("what-s-new", Article.toSlug("What's New"));
    assertEquals("comma-separated-values", Article.toSlug("comma, separated, values"));
    assertEquals("question-mark", Article.toSlug("question? mark"));
  }

  @Test
  void shouldGenerateUniqueIdsForDifferentArticles() {
    Article article1 =
        new Article("Title One", "desc1", "body1", Collections.emptyList(), "user-1");
    Article article2 =
        new Article("Title Two", "desc2", "body2", Collections.emptyList(), "user-2");

    assertNotEquals(article1.getId(), article2.getId());
  }

  @Test
  void shouldDeduplicateTags() {
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java", "java", "spring"), "user-1");

    assertEquals(2, article.getTags().size());
  }

  @Test
  void shouldUpdateArticleFields() {
    DateTime originalTime = new DateTime();
    Article article =
        new Article("Original Title", "Original desc", "Original body", Collections.emptyList(), "user-1", originalTime);

    article.update("New Title", "New desc", "New body");

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("New desc", article.getDescription());
    assertEquals("New body", article.getBody());
    assertTrue(article.getUpdatedAt().isAfter(originalTime) || article.getUpdatedAt().isEqual(originalTime));
  }

  @Test
  void shouldNotUpdateFieldsWhenNullOrEmpty() {
    Article article =
        new Article("Original Title", "Original desc", "Original body", Collections.emptyList(), "user-1");

    article.update(null, null, null);

    assertEquals("Original Title", article.getTitle());
    assertEquals("original-title", article.getSlug());
    assertEquals("Original desc", article.getDescription());
    assertEquals("Original body", article.getBody());
  }

  @Test
  void shouldCreateArticleWithSpecificCreatedAt() {
    DateTime specificTime = new DateTime(2023, 1, 15, 10, 30, 0);
    Article article =
        new Article("Title", "desc", "body", Collections.emptyList(), "user-1", specificTime);

    assertEquals(specificTime, article.getCreatedAt());
    assertEquals(specificTime, article.getUpdatedAt());
  }
}
