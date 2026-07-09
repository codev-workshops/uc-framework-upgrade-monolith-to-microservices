package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.client.FavoriteClient;
import io.spring.client.ProfileClient;
import io.spring.client.UserAuthClient;
import io.spring.client.dto.AuthorRef;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Article read-model composer. Loads base articles + tags from the own DB, then composes the
 * cross-service fields — author projection (user-auth), favoritesCount/favorited (favorite),
 * author.following (profile) — via synchronous HTTP fan-out per the frozen composition strategy.
 */
@Service
@AllArgsConstructor
public class ArticleQueryService {
  private final ArticleReadService articleReadService;
  private final UserAuthClient userAuthClient;
  private final FavoriteClient favoriteClient;
  private final ProfileClient profileClient;

  public Optional<ArticleData> findById(String id, User user) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    compose(new ArrayList<>(List.of(articleData)), user);
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug, User user) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    compose(new ArrayList<>(List.of(articleData)), user);
    return Optional.of(articleData);
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    String authorId = null;
    if (author != null) {
      Optional<AuthorRef> ref = userAuthClient.findByUsername(author);
      if (!ref.isPresent()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      authorId = ref.get().getId();
    }

    List<String> favoritedArticleIds = null;
    if (favoritedBy != null) {
      Optional<AuthorRef> ref = userAuthClient.findByUsername(favoritedBy);
      if (!ref.isPresent()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      favoritedArticleIds = favoriteClient.articlesFavoritedByUser(ref.get().getId());
      if (favoritedArticleIds.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
    }

    List<String> articleIds =
        articleReadService.queryArticles(tag, authorId, favoritedArticleIds, page);
    int articleCount = articleReadService.countArticle(tag, authorId, favoritedArticleIds);
    if (articleIds.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    }
    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    compose(articles, currentUser);
    return new ArticleDataList(articles, articleCount);
  }

  public ArticleDataList findUserFeed(User user, Page page) {
    List<String> followedUsers = profileClient.followedAuthors(user.getId());
    if (followedUsers.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }
    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
    compose(articles, user);
    int count = articleReadService.countFeedSize(followedUsers);
    return new ArticleDataList(articles, count);
  }

  private void compose(List<ArticleData> articles, User currentUser) {
    if (articles.isEmpty()) {
      return;
    }
    fillAuthors(articles);
    fillFavoritesCount(articles);
    if (currentUser != null) {
      fillFavorited(articles, currentUser);
      fillFollowing(articles, currentUser);
    }
  }

  private void fillAuthors(List<ArticleData> articles) {
    List<String> authorIds =
        articles.stream().map(a -> a.getProfileData().getId()).distinct().collect(toList());
    Map<String, AuthorRef> authors = new HashMap<>();
    userAuthClient.findByIds(authorIds).forEach(ref -> authors.put(ref.getId(), ref));
    articles.forEach(
        article -> {
          ProfileData profile = article.getProfileData();
          AuthorRef ref = authors.get(profile.getId());
          if (ref != null) {
            profile.setUsername(ref.getUsername());
            profile.setBio(ref.getBio());
            profile.setImage(ref.getImage());
          }
        });
  }

  private void fillFavoritesCount(List<ArticleData> articles) {
    List<String> articleIds = articles.stream().map(ArticleData::getId).collect(toList());
    Map<String, Integer> countMap = new HashMap<>();
    for (ArticleFavoriteCount item : favoriteClient.countsForArticles(articleIds)) {
      countMap.put(item.getId(), item.getCount());
    }
    articles.forEach(
        article -> article.setFavoritesCount(countMap.getOrDefault(article.getId(), 0)));
  }

  private void fillFavorited(List<ArticleData> articles, User currentUser) {
    List<String> articleIds = articles.stream().map(ArticleData::getId).collect(toList());
    Set<String> favorited =
        new HashSet<>(favoriteClient.favoritedArticles(currentUser.getId(), articleIds));
    articles.forEach(
        article -> {
          if (favorited.contains(article.getId())) {
            article.setFavorited(true);
          }
        });
  }

  private void fillFollowing(List<ArticleData> articles, User currentUser) {
    List<String> authorIds =
        articles.stream().map(a -> a.getProfileData().getId()).distinct().collect(toList());
    Set<String> following =
        new HashSet<>(profileClient.followingAmong(currentUser.getId(), authorIds));
    articles.forEach(
        article -> {
          if (following.contains(article.getProfileData().getId())) {
            article.getProfileData().setFollowing(true);
          }
        });
  }
}
