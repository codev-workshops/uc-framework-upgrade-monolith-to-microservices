package io.spring.infrastructure.favorite;

import io.spring.application.FavoriteQueryService;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({FavoriteQueryService.class})
public class ArticleFavoritesReadServiceTest extends DbTestBase {
  @Autowired private ArticleFavoriteMapper articleFavoriteMapper;
  @Autowired private ArticleFavoritesReadService readService;
  @Autowired private FavoriteQueryService favoriteQueryService;

  @BeforeEach
  public void setUp() {
    articleFavoriteMapper.insert(new ArticleFavorite("article-1", "user-2"));
    articleFavoriteMapper.insert(new ArticleFavorite("article-1", "user-3"));
    articleFavoriteMapper.insert(new ArticleFavorite("article-2", "user-1"));
  }

  @Test
  public void should_count_single_article_favorites() {
    Assertions.assertEquals(2, readService.articleFavoriteCount("article-1"));
    Assertions.assertEquals(0, readService.articleFavoriteCount("article-9"));
  }

  @Test
  public void should_report_favorited_state() {
    Assertions.assertTrue(favoriteQueryService.isFavorited("user-2", "article-1"));
    Assertions.assertFalse(favoriteQueryService.isFavorited("user-1", "article-1"));
  }

  @Test
  public void should_batch_count_and_default_missing_to_zero() {
    List<String> ids = Arrays.asList("article-1", "article-2", "article-9");
    Map<String, Integer> counts = favoriteQueryService.favoriteCounts(ids);
    Assertions.assertEquals(2, counts.get("article-1"));
    Assertions.assertEquals(1, counts.get("article-2"));
    Assertions.assertEquals(0, counts.get("article-9"));
  }

  @Test
  public void should_return_user_favorites_subset() {
    List<String> ids = Arrays.asList("article-1", "article-2");
    Set<String> favorites = favoriteQueryService.userFavorites(ids, "user-1");
    Assertions.assertEquals(1, favorites.size());
    Assertions.assertTrue(favorites.contains("article-2"));

    List<ArticleFavoriteCount> raw = readService.articlesFavoriteCount(ids);
    Assertions.assertEquals(2, raw.size());
  }
}
