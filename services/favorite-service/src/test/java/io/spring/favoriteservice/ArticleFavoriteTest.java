package io.spring.favoriteservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.spring.favoriteservice.domain.ArticleFavorite;
import org.junit.jupiter.api.Test;

class ArticleFavoriteTest {

  @Test
  void shouldCreateArticleFavoriteWithArticleIdAndUserId() {
    ArticleFavorite favorite = new ArticleFavorite("article-123", "user-456");

    assertNotNull(favorite);
    assertEquals("article-123", favorite.getArticleId());
    assertEquals("user-456", favorite.getUserId());
  }

  @Test
  void shouldCreateArticleFavoriteWithNoArgConstructor() {
    ArticleFavorite favorite = new ArticleFavorite();

    assertNotNull(favorite);
    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  void shouldBeEqualWhenSameArticleIdAndUserId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-123", "user-456");

    assertEquals(favorite1, favorite2);
    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenDifferentArticleId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-789", "user-456");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  void shouldNotBeEqualWhenDifferentUserId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-123", "user-789");

    assertNotEquals(favorite1, favorite2);
  }
}
