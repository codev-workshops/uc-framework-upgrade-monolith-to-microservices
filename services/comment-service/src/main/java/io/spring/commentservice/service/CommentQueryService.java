package io.spring.commentservice.service;

import io.spring.commentservice.infrastructure.CommentReadService;
import io.spring.shared.client.UserServiceClient;
import io.spring.shared.data.ProfileData;
import io.spring.shared.data.UserData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String currentUserId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    fillAuthorProfile(commentData);
    if (currentUserId != null && commentData.getProfileData() != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userServiceClient.isFollowing(currentUserId, commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String currentUserId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    comments.forEach(this::fillAuthorProfile);
    if (comments.size() > 0 && currentUserId != null) {
      Set<String> followingAuthors =
          userServiceClient.followingAuthors(
              currentUserId,
              comments.stream()
                  .filter(c -> c.getProfileData() != null)
                  .map(c -> c.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null
                && followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  private void fillAuthorProfile(CommentData commentData) {
    if (commentData.getUserId() != null) {
      userServiceClient
          .findUserById(commentData.getUserId())
          .ifPresent(
              userData ->
                  commentData.setProfileData(
                      new ProfileData(
                          userData.getId(),
                          userData.getUsername(),
                          userData.getBio(),
                          userData.getImage(),
                          false)));
    }
  }
}
