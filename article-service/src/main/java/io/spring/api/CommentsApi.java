package io.spring.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.client.CommentGateway;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public comment routes. article-service owns slug-&gt;id resolution and delegates to
 * comment-service internal endpoints, returning the exact RealWorld comment shapes.
 */
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private final ArticleRepository articleRepository;
  private final CommentGateway commentGateway;

  @GetMapping
  public ResponseEntity<?> getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    JsonNode comments = commentGateway.findByArticleId(article.getId(), userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    JsonNode comment =
        commentGateway.createComment(article.getId(), newCommentParam.getBody(), userId);
    return ResponseEntity.status(201)
        .body(
            new HashMap<String, Object>() {
              {
                put("comment", comment);
              }
            });
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal String userId) {
    articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    commentGateway.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }
}
