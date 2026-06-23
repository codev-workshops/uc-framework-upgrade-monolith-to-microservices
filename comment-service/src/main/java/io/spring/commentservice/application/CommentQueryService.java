package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.UserData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, UserData user) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    enrichProfileData(commentData);
    if (user != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userServiceClient.isFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, UserData user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    comments.forEach(this::enrichProfileData);
    if (comments.size() > 0 && user != null) {
      Map<String, Boolean> followingMap =
          userServiceClient.getFollowingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      Set<String> followingAuthors =
          followingMap.entrySet().stream()
              .filter(Map.Entry::getValue)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, UserData user, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    comments.forEach(this::enrichProfileData);
    if (user != null) {
      Map<String, Boolean> followingMap =
          userServiceClient.getFollowingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      Set<String> followingAuthors =
          followingMap.entrySet().stream()
              .filter(Map.Entry::getValue)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private void enrichProfileData(CommentData commentData) {
    ProfileData profile = commentData.getProfileData();
    if (profile != null && profile.getId() != null) {
      Optional<io.spring.shared.dto.UserData> userOpt =
          userServiceClient.getUserById(profile.getId());
      userOpt.ifPresent(
          userData -> {
            profile.setUsername(userData.getUsername());
            profile.setBio(userData.getBio());
            profile.setImage(userData.getImage());
          });
    }
  }
}
