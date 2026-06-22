package io.spring.comment.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.comment.api.exception.NoAuthorizationException;
import io.spring.comment.api.exception.ResourceNotFoundException;
import io.spring.comment.application.CommentQueryService;
import io.spring.comment.application.data.CommentData;
import io.spring.comment.client.ArticleServiceClient;
import io.spring.comment.core.Comment;
import io.spring.comment.core.CommentRepository;
import io.spring.shared.auth.UserPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleServiceClient articleServiceClient;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Map<String, String> article = articleServiceClient.getArticleBySlug(slug);
    if (article == null || article.isEmpty()) {
      throw new ResourceNotFoundException();
    }
    String articleId = article.get("id");
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), articleId);
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user.getId()).get()));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserPrincipal user) {
    Map<String, String> article = articleServiceClient.getArticleBySlug(slug);
    if (article == null || article.isEmpty()) {
      throw new ResourceNotFoundException();
    }
    String articleId = article.get("id");
    String userId = user != null ? user.getId() : null;
    List<CommentData> comments = commentQueryService.findByArticleId(articleId, userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal UserPrincipal user) {
    Map<String, String> article = articleServiceClient.getArticleBySlug(slug);
    if (article == null || article.isEmpty()) {
      throw new ResourceNotFoundException();
    }
    String articleId = article.get("id");
    String articleUserId = article.get("userId");
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              if (!user.getId().equals(comment.getUserId())
                  && !user.getId().equals(articleUserId)) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
