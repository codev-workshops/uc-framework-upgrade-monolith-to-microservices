package io.spring.favoriteservice.application;

import io.spring.common.data.ArticleData;
import io.spring.common.data.ProfileData;
import io.spring.common.data.UserData;
import io.spring.favoriteservice.client.ArticleServiceClient;
import io.spring.favoriteservice.client.UserServiceClient;
import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleQueryService {

  private final ArticleServiceClient articleServiceClient;
  private final UserServiceClient userServiceClient;
  private final ArticleFavoritesReadService articleFavoritesReadService;

  public ArticleQueryService(
      ArticleServiceClient articleServiceClient,
      UserServiceClient userServiceClient,
      ArticleFavoritesReadService articleFavoritesReadService) {
    this.articleServiceClient = articleServiceClient;
    this.userServiceClient = userServiceClient;
    this.articleFavoritesReadService = articleFavoritesReadService;
  }

  public Optional<ArticleData> findBySlug(String slug, String currentUserId) {
    ArticleData articleData = articleServiceClient.getArticleBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    fillFavoriteAndAuthorInfo(articleData, currentUserId);
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findById(String articleId, String currentUserId) {
    ArticleData articleData = articleServiceClient.getArticleById(articleId);
    if (articleData == null) {
      return Optional.empty();
    }
    fillFavoriteAndAuthorInfo(articleData, currentUserId);
    return Optional.of(articleData);
  }

  private void fillFavoriteAndAuthorInfo(ArticleData articleData, String currentUserId) {
    int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleData.getId());
    articleData.setFavoritesCount(favoriteCount);

    if (currentUserId != null) {
      boolean favorited =
          articleFavoritesReadService.isUserFavorite(currentUserId, articleData.getId());
      articleData.setFavorited(favorited);
    } else {
      articleData.setFavorited(false);
    }

    ProfileData profileData = articleData.getProfileData();
    if (profileData == null && articleData.getProfileData() == null) {
      profileData = fetchAuthorProfile(articleData, currentUserId);
      articleData.setProfileData(profileData);
    } else if (profileData != null && currentUserId != null) {
      boolean following = userServiceClient.isUserFollowing(currentUserId, profileData.getId());
      profileData.setFollowing(following);
    }
  }

  private ProfileData fetchAuthorProfile(ArticleData articleData, String currentUserId) {
    UserData author = userServiceClient.getUserById(articleData.getId());
    if (author == null) {
      return null;
    }
    boolean following = false;
    if (currentUserId != null) {
      following = userServiceClient.isUserFollowing(currentUserId, author.getId());
    }
    return new ProfileData(
        author.getId(), author.getUsername(), author.getBio(), author.getImage(), following);
  }
}
