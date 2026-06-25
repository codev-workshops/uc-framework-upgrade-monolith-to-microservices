package io.spring.commentservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.spring.commentservice.domain.Comment;
import org.junit.jupiter.api.Test;

class CommentTest {

  @Test
  void shouldCreateCommentWithGeneratedIdAndTimestamp() {
    Comment comment = new Comment("test body", "user-1", "article-1");

    assertNotNull(comment.getId());
    assertEquals("test body", comment.getBody());
    assertEquals("user-1", comment.getUserId());
    assertEquals("article-1", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldGenerateUniqueIds() {
    Comment c1 = new Comment("body1", "user-1", "article-1");
    Comment c2 = new Comment("body2", "user-1", "article-1");

    assertNotNull(c1.getId());
    assertNotNull(c2.getId());
    assertNotEquals(c1.getId(), c2.getId());
  }

  @Test
  void shouldUseEqualsBasedOnId() {
    Comment c1 = new Comment("body", "user-1", "article-1");
    Comment c2 = new Comment("body", "user-1", "article-1");

    assertNotEquals(c1, c2, "Different instances should have different generated IDs");
  }

  private static void assertNotEquals(Object a, Object b) {
    if (a.equals(b)) {
      throw new AssertionError("Expected objects to be not equal but they were: " + a);
    }
  }

  private static void assertNotEquals(Object a, Object b, String message) {
    if (a.equals(b)) {
      throw new AssertionError(message + ": " + a);
    }
  }
}
