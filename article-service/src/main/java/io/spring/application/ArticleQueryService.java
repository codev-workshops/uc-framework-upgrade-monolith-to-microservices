package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.contracts.client.FavoriteServiceClient;
import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.client.TagServiceClient;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Read composition that replaces the monolith's {@code ArticleReadService.xml} joins and {@code
 * fillExtraInfo}/{@code setIsFollowingAuthor}: base rows come from the {@code articles} table this
 * service owns, while tags, favorites, author profile and follow state are fanned out to
 * tag/favorite/profile/user services. Response shapes and offset/limit pagination match the
 * monolith so the frontend and Selenium tests pass unchanged.
 */
@Service
@AllArgsConstructor
public class ArticleQueryService {
  private final ArticleReadService articleReadService;
  private final UserServiceClient userServiceClient;
  private final TagServiceClient tagServiceClient;
  private final FavoriteServiceClient favoriteServiceClient;
  private final ProfileServiceClient profileServiceClient;

  public Optional<ArticleData> findById(String id, String currentUserId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    enrichSingle(articleData, currentUserId);
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug, String currentUserId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    enrichSingle(articleData, currentUserId);
    return Optional.of(articleData);
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String currentUserId) {
    String authorId = null;
    if (author != null) {
      Optional<UserSummary> authorUser = userServiceClient.findByUsername(author);
      if (authorUser.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      authorId = authorUser.get().getId();
    }

    List<ArticleData> candidates =
        authorId == null
            ? articleReadService.findAll()
            : articleReadService.findByAuthors(List.of(authorId));

    if (tag != null) {
      candidates = filterByTag(candidates, tag);
    }
    if (favoritedBy != null) {
      Optional<UserSummary> favUser = userServiceClient.findByUsername(favoritedBy);
      if (favUser.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      candidates = filterByFavoritedBy(candidates, favUser.get().getId());
    }

    int count = candidates.size();
    List<ArticleData> articles = paginate(candidates, page);
    fillExtraInfo(articles, currentUserId);
    return new ArticleDataList(articles, count);
  }

  public ArticleDataList findUserFeed(String currentUserId, Page page) {
    List<String> followedUsers = profileServiceClient.followedUsers(currentUserId);
    if (followedUsers.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }
    List<ArticleData> candidates = articleReadService.findByAuthors(followedUsers);
    int count = candidates.size();
    List<ArticleData> articles = paginate(candidates, page);
    fillExtraInfo(articles, currentUserId);
    return new ArticleDataList(articles, count);
  }

  private List<ArticleData> filterByTag(List<ArticleData> articles, String tag) {
    if (articles.isEmpty()) {
      return articles;
    }
    Map<String, List<String>> tagsByArticle =
        tagServiceClient.tagsOfArticles(
            articles.stream().map(ArticleData::getId).collect(toList()));
    return articles.stream()
        .filter(a -> tagsByArticle.getOrDefault(a.getId(), List.of()).contains(tag))
        .collect(toList());
  }

  private List<ArticleData> filterByFavoritedBy(List<ArticleData> articles, String userId) {
    if (articles.isEmpty()) {
      return articles;
    }
    Set<String> favorited =
        favoriteServiceClient.userFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()), userId);
    return articles.stream().filter(a -> favorited.contains(a.getId())).collect(toList());
  }

  private List<ArticleData> paginate(List<ArticleData> articles, Page page) {
    int from = Math.min(page.getOffset(), articles.size());
    int to = Math.min(from + page.getLimit(), articles.size());
    return new ArrayList<>(articles.subList(from, to));
  }

  private void enrichSingle(ArticleData article, String currentUserId) {
    List<ArticleData> singleton = Collections.singletonList(article);
    setTagList(singleton);
    setAuthor(singleton, currentUserId);
    if (currentUserId != null) {
      article.setFavorited(favoriteServiceClient.isFavorited(currentUserId, article.getId()));
      article.setFavoritesCount(favoriteServiceClient.articleFavoriteCount(article.getId()));
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    if (articles.isEmpty()) {
      return;
    }
    setTagList(articles);
    setAuthor(articles, currentUserId);
    setFavoriteCount(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
    }
  }

  private void setTagList(List<ArticleData> articles) {
    Map<String, List<String>> tagsByArticle =
        tagServiceClient.tagsOfArticles(
            articles.stream().map(ArticleData::getId).collect(toList()));
    articles.forEach(a -> a.setTagList(tagsByArticle.getOrDefault(a.getId(), new ArrayList<>())));
  }

  private void setAuthor(List<ArticleData> articles, String currentUserId) {
    List<String> authorIds =
        articles.stream().map(a -> a.getProfileData().getId()).distinct().collect(toList());
    Map<String, UserSummary> authors =
        userServiceClient.findByIds(authorIds).stream()
            .collect(Collectors.toMap(UserSummary::getId, Function.identity(), (a, b) -> a));
    Set<String> followingAuthors =
        currentUserId == null
            ? Set.of()
            : profileServiceClient.followingAuthors(currentUserId, new ArrayList<>(authorIds));
    articles.forEach(
        article -> {
          String authorId = article.getProfileData().getId();
          UserSummary author = authors.get(authorId);
          if (author == null) {
            article.setProfileData(new ProfileData(authorId, null, null, null, false));
          } else {
            article.setProfileData(
                new ProfileData(
                    author.getId(),
                    author.getUsername(),
                    author.getBio(),
                    author.getImage(),
                    followingAuthors.contains(author.getId())));
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    Map<String, Integer> counts =
        favoriteServiceClient.favoriteCounts(
            articles.stream().map(ArticleData::getId).collect(toList()));
    articles.forEach(a -> a.setFavoritesCount(counts.getOrDefault(a.getId(), 0)));
  }

  private void setIsFavorite(List<ArticleData> articles, String currentUserId) {
    Set<String> favorited =
        favoriteServiceClient.userFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()), currentUserId);
    articles.forEach(a -> a.setFavorited(favorited.contains(a.getId())));
  }
}
