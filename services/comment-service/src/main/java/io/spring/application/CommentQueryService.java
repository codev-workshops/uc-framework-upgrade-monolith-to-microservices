package io.spring.application;

import io.spring.application.clients.ProfileClient;
import io.spring.application.clients.UserAuthClient;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private final io.spring.infrastructure.mybatis.readservice.CommentReadService commentReadService;
  private final UserAuthClient userAuthClient;
  private final ProfileClient profileClient;

  public Optional<CommentData> findById(String id, String currentUserId, String authorization) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    enrichAuthor(commentData, authorization);
    if (currentUserId != null) {
      Set<String> following =
          profileClient.followingAmong(
              currentUserId,
              Collections.singletonList(commentData.getProfileData().getId()),
              authorization);
      commentData
          .getProfileData()
          .setFollowing(following.contains(commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(
      String articleId, String currentUserId, String authorization) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    comments.forEach(commentData -> enrichAuthor(commentData, authorization));
    if (!comments.isEmpty() && currentUserId != null) {
      Set<String> followingAuthors =
          profileClient.followingAmong(
              currentUserId,
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()),
              authorization);
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  private void enrichAuthor(CommentData commentData, String authorization) {
    ProfileData profileData = commentData.getProfileData();
    if (profileData == null || profileData.getId() == null) {
      return;
    }
    userAuthClient
        .findById(profileData.getId(), authorization)
        .ifPresent(
            author -> {
              profileData.setUsername(author.getUsername());
              profileData.setBio(author.getBio());
              profileData.setImage(author.getImage());
            });
  }
}
