package io.spring.articleservice.api;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
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
@RequestMapping(path = "/internal/articles")
@AllArgsConstructor
public class InternalArticleApi {
  private ArticleReadService articleReadService;

  @GetMapping("/by-slug/{slug}")
  public ResponseEntity<?> getArticleBySlug(@PathVariable("slug") String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(articleResponse(articleData));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getArticleById(@PathVariable("id") String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(articleResponse(articleData));
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
