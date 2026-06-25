package io.spring.articleservice.api;

import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleRepository;
import io.spring.shared.exception.ResourceNotFoundException;
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

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getArticleById(@PathVariable("id") String id) {
    Article article = articleRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(toMap(article));
  }

  @GetMapping("/by-slug/{slug}")
  public ResponseEntity<Map<String, Object>> getArticleBySlug(@PathVariable("slug") String slug) {
    Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(toMap(article));
  }

  private Map<String, Object> toMap(Article article) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", article.getId());
    map.put("slug", article.getSlug());
    map.put("title", article.getTitle());
    map.put("userId", article.getUserId());
    return map;
  }
}
