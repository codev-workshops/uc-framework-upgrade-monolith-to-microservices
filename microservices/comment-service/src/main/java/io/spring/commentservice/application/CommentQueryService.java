package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
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
    } else {
      fillExtraInfo(commentData, currentUserId);
      return Optional.of(commentData);
    }
  }

  public List<CommentData> findByArticleId(String articleId, String currentUserId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (!comments.isEmpty()) {
      fillExtraInfo(comments, currentUserId);
    }
    return comments;
  }

  private void fillExtraInfo(CommentData commentData, String currentUserId) {
    ProfileData profileData = userServiceClient.getProfile(commentData.getUserId());
    if (profileData != null) {
      if (currentUserId != null) {
        profileData.setFollowing(
            userServiceClient.isUserFollowing(currentUserId, profileData.getId()));
      }
      commentData.setProfileData(profileData);
    }
  }

  private void fillExtraInfo(List<CommentData> comments, String currentUserId) {
    List<String> authorIds =
        comments.stream().map(CommentData::getUserId).collect(Collectors.toList());
    Set<String> followingAuthors =
        currentUserId != null
            ? userServiceClient.followingAuthors(currentUserId, authorIds)
            : java.util.Collections.emptySet();

    for (CommentData comment : comments) {
      ProfileData profileData = userServiceClient.getProfile(comment.getUserId());
      if (profileData != null) {
        profileData.setFollowing(followingAuthors.contains(profileData.getId()));
        comment.setProfileData(profileData);
      }
    }
  }
}
