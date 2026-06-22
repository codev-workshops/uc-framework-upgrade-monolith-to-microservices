package io.spring.commentservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.commentservice.api.exception.NoAuthorizationException;
import io.spring.commentservice.api.exception.ResourceNotFoundException;
import io.spring.commentservice.application.CommentQueryService;
import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import io.spring.common.client.ArticleServiceClient;
import io.spring.common.dto.ArticleIdResponse;
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
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    ArticleIdResponse article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    Comment comment = new Comment(newCommentParam.getBody(), userId, article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), userId).get()));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleIdResponse article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), userId);
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
      @AuthenticationPrincipal String userId) {
    ArticleIdResponse article = articleServiceClient.getArticleBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!canWriteComment(userId, article.getUserId(), comment)) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private boolean canWriteComment(String userId, String articleUserId, Comment comment) {
    return userId != null
        && (userId.equals(articleUserId) || userId.equals(comment.getUserId()));
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
