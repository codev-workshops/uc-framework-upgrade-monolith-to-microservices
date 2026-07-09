package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.clients.ArticleClient;
import io.spring.application.data.ArticleRef;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.security.CurrentUser;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private final ArticleClient articleClient;
  private final CommentRepository commentRepository;
  private final CommentQueryService commentQueryService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal CurrentUser user,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    ArticleRef article =
        articleClient.findBySlug(slug, authorization).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(
            commentResponse(
                commentQueryService.findById(comment.getId(), user.getId(), authorization).get()));
  }

  @GetMapping
  public ResponseEntity<?> getComments(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal CurrentUser user,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    ArticleRef article =
        articleClient.findBySlug(slug, authorization).orElseThrow(ResourceNotFoundException::new);
    String currentUserId = user == null ? null : user.getId();
    List<CommentData> comments =
        commentQueryService.findByArticleId(article.getId(), currentUserId, authorization);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal CurrentUser user,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    ArticleRef article =
        articleClient.findBySlug(slug, authorization).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user.getId(), article, comment)) {
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
