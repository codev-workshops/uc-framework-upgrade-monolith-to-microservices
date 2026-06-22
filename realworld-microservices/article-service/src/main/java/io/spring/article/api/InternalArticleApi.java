package io.spring.article.api;

import io.spring.article.core.Article;
import io.spring.article.core.ArticleRepository;
import java.util.HashMap;
import java.util.Map;
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

  private ArticleRepository articleRepository;

  @GetMapping("/by-slug/{slug}")
  public ResponseEntity<?> getArticleBySlug(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Map<String, Object> result = new HashMap<>();
              result.put("id", article.getId());
              result.put("userId", article.getUserId());
              result.put("slug", article.getSlug());
              result.put("title", article.getTitle());
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getArticleById(@PathVariable("id") String id) {
    return articleRepository
        .findById(id)
        .map(
            article -> {
              Map<String, Object> result = new HashMap<>();
              result.put("id", article.getId());
              result.put("userId", article.getUserId());
              result.put("slug", article.getSlug());
              result.put("title", article.getTitle());
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }
}
