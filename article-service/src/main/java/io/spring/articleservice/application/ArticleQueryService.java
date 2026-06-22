package io.spring.articleservice.application;

import static java.util.stream.Collectors.toList;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.Page;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.common.client.FavoriteServiceClient;
import io.spring.common.client.UserServiceClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
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
    List<String> followedUsers = userServiceClient.followedUsers(userId);
    if (followedUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
      fillExtraInfo(articles, userId);
      int count = articleReadService.countFeedSize(followedUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    setFavoriteCount(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
      setIsFollowingAuthor(articles, currentUserId);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String currentUserId) {
    List<String> authorIds =
        articles.stream()
            .map(articleData -> articleData.getProfileData().getId())
            .collect(toList());
    Set<String> followingAuthors = userServiceClient.followingAuthors(currentUserId, authorIds);
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<String> articleIds = articles.stream().map(ArticleData::getId).collect(toList());
    Map<String, Integer> countMap = favoriteServiceClient.getFavoriteCounts(articleIds);
    articles.forEach(
        articleData -> {
          Integer count = countMap.get(articleData.getId());
          articleData.setFavoritesCount(count != null ? count : 0);
        });
  }

  private void setIsFavorite(List<ArticleData> articles, String currentUserId) {
    List<String> articleIds =
        articles.stream().map(ArticleData::getId).collect(toList());
    Set<String> favoritedArticles = favoriteServiceClient.isUserFavorite(currentUserId, articleIds);
    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void fillExtraInfo(String id, String userId, ArticleData articleData) {
    articleData.setFavorited(
        favoriteServiceClient.isUserFavoriteSingle(userId, id));
    articleData.setFavoritesCount(
        favoriteServiceClient.articleFavoriteCount(id));
    articleData
        .getProfileData()
        .setFollowing(
            userServiceClient.isFollowing(userId, articleData.getProfileData().getId()));
  }
}
