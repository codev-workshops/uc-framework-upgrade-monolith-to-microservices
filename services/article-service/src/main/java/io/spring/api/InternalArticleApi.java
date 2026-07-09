package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints (NOT exposed by the gateway).
 *
 * <ul>
 *   <li>{@code GET /internal/articles/{slug}} resolves slug -> {id, slug, authorId} for comment
 *       authorization and favorite id mapping.
 *   <li>{@code GET /internal/articles/{id}/data} recomposes the full ArticleData for the current
 *       user (used by the gateway after favorite/unfavorite).
 * </ul>
 */
@RestController
@RequestMapping(path = "/internal/articles")
@AllArgsConstructor
public class InternalArticleApi {
  private final ArticleRepository articleRepository;
  private final ArticleQueryService articleQueryService;

  @GetMapping("/{slug}")
  public ResponseEntity<ArticleRef> resolveBySlug(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                ResponseEntity.ok(
                    new ArticleRef(article.getId(), article.getSlug(), article.getUserId())))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping("/{id}/data")
  public ResponseEntity<Map<String, Object>> recomposeById(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findById(id, user)
        .map(this::articleResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ResponseEntity<Map<String, Object>> articleResponse(ArticleData articleData) {
    Map<String, Object> response = new HashMap<>();
    response.put("article", articleData);
    return ResponseEntity.ok(response);
  }
}
