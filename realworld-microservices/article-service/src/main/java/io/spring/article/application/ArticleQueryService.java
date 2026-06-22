package io.spring.article.application;

import static java.util.stream.Collectors.toList;

import io.spring.article.client.FavoriteServiceClient;
import io.spring.article.client.UserServiceClient;
import io.spring.article.infrastructure.ArticleReadService;
import io.spring.shared.dto.CursorPageParameter;
import io.spring.shared.dto.CursorPager;
import io.spring.shared.dto.Page;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;
  private FavoriteServiceClient favoriteServiceClient;

  public Optional<ArticleData> findById(String id, String userId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (userId != null) {
        fillExtraInfo(id, userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, String userId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (userId != null) {
        fillExtraInfo(articleData.getId(), userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      String userId) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      boolean hasExtra = articleIds.size() > page.getLimit();
      if (hasExtra) {
        articleIds.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articleIds);
      }

      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, userId);

      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public CursorPager<ArticleData> findUserFeedWithCursor(
      String userId, CursorPageParameter<DateTime> page) {
    List<String> followedUsers = userServiceClient.getFollowedUsers(userId);
    if (followedUsers.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followedUsers, page);
      boolean hasExtra = articles.size() > page.getLimit();
      if (hasExtra) {
        articles.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articles);
      }
      fillExtraInfo(articles, userId);
      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String userId) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, userId);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(String userId, Page page) {
    List<String> followedUsers = userServiceClient.getFollowedUsers(userId);
    if (followedUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthors(followedUsers, page);
      fillExtraInfo(articles, userId);
      int count = articleReadService.countFeedSize(followedUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String userId) {
    setFavoriteCount(articles);
    if (userId != null) {
      setIsFavorite(articles, userId);
      setIsFollowingAuthor(articles, userId);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String userId) {
    Set<String> followingAuthors =
        userServiceClient.getFollowingAuthors(
            userId,
            articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
                .collect(toList()));
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    Map<String, Integer> countMap =
        favoriteServiceClient.getFavoriteCounts(
            articles.stream().map(ArticleData::getId).collect(toList()));
    articles.forEach(
        articleData -> {
          Integer count = countMap.get(articleData.getId());
          if (count != null) {
            articleData.setFavoritesCount(count);
          }
        });
  }

  private void setIsFavorite(List<ArticleData> articles, String userId) {
    Set<String> favoritedArticles =
        favoriteServiceClient.getUserFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()),
            userId);

    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void fillExtraInfo(String id, String userId, ArticleData articleData) {
    articleData.setFavorited(favoriteServiceClient.isFavorited(id, userId));
    Map<String, Integer> counts =
        favoriteServiceClient.getFavoriteCounts(Collections.singletonList(id));
    Integer count = counts.get(id);
    articleData.setFavoritesCount(count != null ? count : 0);
    if (articleData.getProfileData() != null) {
      Set<String> following =
          userServiceClient.getFollowingAuthors(
              userId,
              Collections.singletonList(articleData.getProfileData().getId()));
      articleData
          .getProfileData()
          .setFollowing(following.contains(articleData.getProfileData().getId()));
    }
  }
}
