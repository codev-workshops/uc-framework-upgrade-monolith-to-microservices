package io.spring.articleservice.service;

import static java.util.stream.Collectors.toList;

import io.spring.shared.client.FavoriteServiceClient;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.data.ArticleFavoriteCount;
import io.spring.shared.data.ProfileData;
import io.spring.articleservice.infrastructure.ArticleReadService;
import java.util.ArrayList;
import java.util.HashMap;
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

  public Optional<ArticleData> findById(String id, String currentUserId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillAuthorProfile(articleData);
      if (currentUserId != null) {
        fillExtraInfo(id, currentUserId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, String currentUserId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillAuthorProfile(articleData);
      if (currentUserId != null) {
        fillExtraInfo(articleData.getId(), currentUserId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String currentUserId) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUserId);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(String userId, Page page) {
    List<String> followedUsers = userServiceClient.followedUsers(userId);
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

  private void fillAuthorProfile(ArticleData articleData) {
    if (articleData.getUserId() != null) {
      userServiceClient
          .findUserById(articleData.getUserId())
          .ifPresent(
              userData ->
                  articleData.setProfileData(
                      new ProfileData(
                          userData.getId(),
                          userData.getUsername(),
                          userData.getBio(),
                          userData.getImage(),
                          false)));
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    articles.forEach(this::fillAuthorProfile);
    setFavoriteCount(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
      setIsFollowingAuthor(articles, currentUserId);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String currentUserId) {
    List<String> authorIds =
        articles.stream()
            .filter(a -> a.getProfileData() != null)
            .map(a -> a.getProfileData().getId())
            .collect(toList());
    if (authorIds.isEmpty()) return;
    Set<String> followingAuthors = userServiceClient.followingAuthors(currentUserId, authorIds);
    articles.forEach(
        articleData -> {
          if (articleData.getProfileData() != null
              && followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        favoriteServiceClient.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(item -> countMap.put(item.getId(), item.getCount()));
    articles.forEach(
        articleData -> {
          Integer count = countMap.get(articleData.getId());
          articleData.setFavoritesCount(count != null ? count : 0);
        });
  }

  private void setIsFavorite(List<ArticleData> articles, String currentUserId) {
    Set<String> favoritedArticles =
        favoriteServiceClient.userFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()), currentUserId);
    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void fillExtraInfo(String id, String userId, ArticleData articleData) {
    articleData.setFavorited(favoriteServiceClient.isUserFavorite(userId, id));
    articleData.setFavoritesCount(favoriteServiceClient.articleFavoriteCount(id));
    if (articleData.getProfileData() != null) {
      articleData
          .getProfileData()
          .setFollowing(
              userServiceClient.isFollowing(userId, articleData.getProfileData().getId()));
    }
  }
}
