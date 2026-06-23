package io.spring.articleservice.application.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.articleservice.api.security.AuthenticatedUser;
import io.spring.articleservice.application.ArticleQueryService;
import io.spring.articleservice.application.Page;
import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.shared.client.FavoriteServiceClient;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private FavoriteServiceClient favoriteServiceClient;
  @Mock private UserServiceClient userServiceClient;

  @InjectMocks private ArticleQueryService queryService;

  private AuthenticatedUser user;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() {
    user = new AuthenticatedUser("user-1", "aisensiy", "aisensiy@gmail.com", "", "");
    articleData =
        new ArticleData(
            "article-1",
            "test",
            "test",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java", "spring"),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
  }

  @Test
  public void should_fetch_article_success() {
    when(articleReadService.findById("article-1")).thenReturn(articleData);
    when(favoriteServiceClient.isUserFavorite(anyString(), anyString())).thenReturn(false);
    when(favoriteServiceClient.getFavoriteCount(anyString())).thenReturn(0);
    when(userServiceClient.isFollowing(anyString(), anyString())).thenReturn(false);

    Optional<ArticleData> optional = queryService.findById("article-1", user);
    Assertions.assertTrue(optional.isPresent());

    ArticleData fetched = optional.get();
    Assertions.assertEquals(fetched.getFavoritesCount(), 0);
    Assertions.assertFalse(fetched.isFavorited());
    Assertions.assertNotNull(fetched.getCreatedAt());
    Assertions.assertNotNull(fetched.getUpdatedAt());
    Assertions.assertTrue(fetched.getTagList().contains("java"));
  }

  @Test
  public void should_get_article_with_right_favorite_and_favorite_count() {
    when(articleReadService.findById("article-1")).thenReturn(articleData);
    when(favoriteServiceClient.isUserFavorite("user-1", "article-1")).thenReturn(true);
    when(favoriteServiceClient.getFavoriteCount("article-1")).thenReturn(1);
    when(userServiceClient.isFollowing(anyString(), anyString())).thenReturn(false);

    Optional<ArticleData> optional = queryService.findById("article-1", user);
    Assertions.assertTrue(optional.isPresent());

    ArticleData result = optional.get();
    Assertions.assertEquals(result.getFavoritesCount(), 1);
    Assertions.assertTrue(result.isFavorited());
  }

  @Test
  public void should_get_default_article_list() {
    ArticleData anotherArticle =
        new ArticleData(
            "article-2",
            "new-article",
            "new article",
            "desc",
            "body",
            false,
            0,
            new DateTime().minusHours(1),
            new DateTime().minusHours(1),
            Arrays.asList("test"),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));

    when(articleReadService.queryArticles(null, null, null, new Page()))
        .thenReturn(Arrays.asList("article-1", "article-2"));
    when(articleReadService.countArticle(null, null, null)).thenReturn(2);
    when(articleReadService.findArticles(Arrays.asList("article-1", "article-2")))
        .thenReturn(Arrays.asList(articleData, anotherArticle));
    when(favoriteServiceClient.getFavoriteCounts(anyList())).thenReturn(Collections.emptyList());
    when(favoriteServiceClient.getUserFavorites(anyList(), anyString()))
        .thenReturn(Collections.emptySet());
    when(userServiceClient.getFollowingAuthors(anyString(), anyList()))
        .thenReturn(Collections.emptyMap());

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getCount(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), "article-1");
  }

  @Test
  public void should_show_following_if_user_followed_author() {
    when(articleReadService.queryArticles(null, null, null, new Page()))
        .thenReturn(Arrays.asList("article-1"));
    when(articleReadService.countArticle(null, null, null)).thenReturn(1);
    when(articleReadService.findArticles(Arrays.asList("article-1")))
        .thenReturn(Arrays.asList(articleData));
    when(favoriteServiceClient.getFavoriteCounts(anyList())).thenReturn(Collections.emptyList());
    when(favoriteServiceClient.getUserFavorites(anyList(), anyString()))
        .thenReturn(Collections.emptySet());

    Map<String, Boolean> followingMap = new HashMap<>();
    followingMap.put(user.getId(), true);
    when(userServiceClient.getFollowingAuthors(anyString(), anyList())).thenReturn(followingMap);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData result = recentArticles.getArticleDatas().get(0);
    Assertions.assertTrue(result.getProfileData().isFollowing());
  }

  @Test
  public void should_get_user_feed() {
    when(userServiceClient.getFollowingAuthors(anyString(), any()))
        .thenReturn(Collections.emptyMap());

    ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
    Assertions.assertEquals(userFeed.getCount(), 0);
  }
}
