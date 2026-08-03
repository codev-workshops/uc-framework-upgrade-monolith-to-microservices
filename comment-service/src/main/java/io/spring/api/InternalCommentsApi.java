package io.spring.api;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inter-service endpoints for comments, keyed by the referenced {@code articleId}: comment-service
 * cannot resolve article slugs, so article-service maps the public {@code
 * /articles/{slug}/comments} route onto these. Also backs the {@code CommentServiceClient} contract
 * ({@code /count}).
 */
@RestController
@RequestMapping(path = "/internal")
@AllArgsConstructor
public class InternalCommentsApi {

  private final CommentQueryService commentQueryService;
  private final CommentRepository commentRepository;

  @GetMapping("/articles/{articleId}/comments")
  public ResponseEntity<List<CommentData>> comments(
      @PathVariable("articleId") String articleId,
      @RequestParam(value = "currentUserId", required = false) String currentUserId) {
    return ResponseEntity.ok(commentQueryService.findByArticleId(articleId, currentUserId));
  }

  @GetMapping("/articles/{articleId}/comments/count")
  public ResponseEntity<Integer> commentCount(@PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(commentQueryService.countByArticleId(articleId));
  }

  @PostMapping("/articles/{articleId}/comments")
  public ResponseEntity<CommentData> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Comment comment =
        new Comment(newCommentParam.getBody(), newCommentParam.getUserId(), articleId);
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            commentQueryService
                .findById(comment.getId(), newCommentParam.getUserId())
                .orElseThrow());
  }

  @GetMapping("/comments/{id}")
  public ResponseEntity<CommentData> comment(
      @PathVariable("id") String id,
      @RequestParam(value = "currentUserId", required = false) String currentUserId) {
    return commentQueryService
        .findById(id, currentUserId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/comments/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    return commentRepository
        .findById(id)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
