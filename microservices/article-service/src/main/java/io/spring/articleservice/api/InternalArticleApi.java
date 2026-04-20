package io.spring.articleservice.api;

import io.spring.articleservice.api.exception.ResourceNotFoundException;
import io.spring.articleservice.application.ArticleQueryService;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/articles")
@AllArgsConstructor
public class InternalArticleApi {
  private ArticleQueryService articleQueryService;

  @GetMapping("/{slug}")
  public ResponseEntity<?> getArticleBySlug(@PathVariable("slug") String slug) {
    return articleQueryService
        .findBySlug(slug, null)
        .map(
            articleData ->
                ResponseEntity.ok(
                    new HashMap<String, Object>() {
                      {
                        put("article", articleData);
                      }
                    }))
        .orElseThrow(ResourceNotFoundException::new);
  }
}
