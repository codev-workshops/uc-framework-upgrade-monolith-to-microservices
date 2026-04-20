package io.spring.articleservice.api;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
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
  public ResponseEntity<Map<String, Object>> getBySlug(@PathVariable("slug") String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> result = new HashMap<>();
    result.put("id", articleData.getId());
    result.put("slug", articleData.getSlug());
    result.put(
        "userId",
        articleData.getProfileData() != null ? articleData.getProfileData().getId() : null);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> result = new HashMap<>();
    result.put("id", articleData.getId());
    result.put("slug", articleData.getSlug());
    result.put(
        "userId",
        articleData.getProfileData() != null ? articleData.getProfileData().getId() : null);
    return ResponseEntity.ok(result);
  }
}
