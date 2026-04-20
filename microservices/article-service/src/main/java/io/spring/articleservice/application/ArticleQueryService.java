package io.spring.articleservice.application;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.ArticleFavoriteCount;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private UserServiceClient userServiceClient;

  public Optional<ArticleData> findById(String id, String currentUserId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
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
      if (currentUserId != null) {
        fillExtraInfo(articleData.getId(), currentUserId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String currentUserId) {
    List<String> articleIds =
        articleReadService.queryArticles(
            tag, author, favoritedBy, page.getOffset(), page.getLimit());
    int count = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), count);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUserId);
      return new ArticleDataList(articles, count);
    }
  }

  public ArticleDataList findUserFeed(String userId, Page page) {
    List<String> followedUsers = userServiceClient.followedUsers(userId);
    if (followedUsers.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthors(
              followedUsers, page.getOffset(), page.getLimit());
      int count = articleReadService.countFeedSize(followedUsers);
      fillExtraInfo(articles, userId);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(String id, String currentUserId, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(currentUserId, id));
    articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
    if (articleData.getProfileData() != null) {
      ProfileData profileData = articleData.getProfileData();
      profileData.setFollowing(
          userServiceClient.isUserFollowing(currentUserId, profileData.getId()));
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    if (articles.isEmpty()) {
      return;
    }
    List<String> ids = articles.stream().map(ArticleData::getId).collect(Collectors.toList());
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(ids);
    Map<String, Integer> countMap = new HashMap<>();
    for (ArticleFavoriteCount count : favoritesCounts) {
      countMap.put(count.getId(), count.getCount());
    }

    Set<String> favorited =
        currentUserId != null
            ? articleFavoritesReadService.userFavorites(ids, currentUserId)
            : Collections.emptySet();

    List<String> authorIds =
        articles.stream()
            .filter(a -> a.getProfileData() != null)
            .map(a -> a.getProfileData().getId())
            .collect(Collectors.toList());
    Set<String> followingAuthors =
        currentUserId != null
            ? userServiceClient.followingAuthors(currentUserId, authorIds)
            : Collections.emptySet();

    for (ArticleData article : articles) {
      article.setFavoritesCount(countMap.getOrDefault(article.getId(), 0));
      article.setFavorited(favorited.contains(article.getId()));
      if (article.getProfileData() != null) {
        article
            .getProfileData()
            .setFollowing(followingAuthors.contains(article.getProfileData().getId()));
      }
    }
  }
}
