package io.spring.commentservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.commentservice.domain.Comment;
import io.spring.commentservice.domain.CommentRepository;
import io.spring.commentservice.service.CommentData;
import io.spring.commentservice.service.CommentQueryService;
import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.data.UserData;
import io.spring.shared.exception.NoAuthorizationException;
import io.spring.shared.exception.ResourceNotFoundException;
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
      @AuthenticationPrincipal UserData user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Map<String, Object> article =
        articleServiceClient.findArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String articleId = (String) article.get("id");
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), articleId);
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user.getId()).get()));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserData user) {
    Map<String, Object> article =
        articleServiceClient.findArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String articleId = (String) article.get("id");
    List<CommentData> comments =
        commentQueryService.findByArticleId(articleId, user != null ? user.getId() : null);
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
      @AuthenticationPrincipal UserData user) {
    Map<String, Object> article =
        articleServiceClient.findArticleBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String articleId = (String) article.get("id");
    String articleUserId = (String) article.get("userId");
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              if (!user.getId().equals(articleUserId)
                  && !user.getId().equals(comment.getUserId())) {
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
