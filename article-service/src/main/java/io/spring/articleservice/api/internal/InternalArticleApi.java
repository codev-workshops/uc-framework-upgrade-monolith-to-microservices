package io.spring.articleservice.api.internal;

import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.common.data.ArticleData;
import io.spring.common.exception.ResourceNotFoundException;
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

  @GetMapping(path = "/by-slug/{slug}")
  public ResponseEntity<ArticleData> getArticleBySlug(@PathVariable("slug") String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(articleData);
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ArticleData> getArticleById(@PathVariable("id") String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      throw new ResourceNotFoundException();
    }
    return ResponseEntity.ok(articleData);
  }
}
