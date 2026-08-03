package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Replaces the monolith's in-process join against {@code users}/{@code follows}: author data comes
 * from user-service and the {@code following} flag from profile-service.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private final CommentReadService commentReadService;
  private final UserServiceClient userServiceClient;
  private final ProfileServiceClient profileServiceClient;

  public Optional<CommentData> findById(String id, String currentUserId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    enrich(List.of(commentData), currentUserId);
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String currentUserId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    enrich(comments, currentUserId);
    return comments;
  }

  public int countByArticleId(String articleId) {
    return commentReadService.countByArticleId(articleId);
  }

  private void enrich(List<CommentData> comments, String currentUserId) {
    if (comments.isEmpty()) {
      return;
    }
    List<String> authorIds =
        comments.stream().map(CommentData::getUserId).distinct().collect(Collectors.toList());
    Map<String, UserSummary> authors =
        userServiceClient.findByIds(authorIds).stream()
            .collect(Collectors.toMap(UserSummary::getId, Function.identity(), (a, b) -> a));
    Set<String> followingAuthors =
        currentUserId == null
            ? Set.of()
            : profileServiceClient.followingAuthors(currentUserId, new ArrayList<>(authorIds));
    comments.forEach(
        comment -> {
          UserSummary author = authors.get(comment.getUserId());
          if (author == null) {
            comment.setProfileData(new ProfileData(comment.getUserId(), null, null, null, false));
          } else {
            comment.setProfileData(
                new ProfileData(
                    author.getId(),
                    author.getUsername(),
                    author.getBio(),
                    author.getImage(),
                    followingAuthors.contains(author.getId())));
          }
        });
  }
}
