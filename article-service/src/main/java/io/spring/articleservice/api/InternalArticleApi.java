package io.spring.articleservice.api;

import io.spring.articleservice.core.ArticleRepository;
import io.spring.common.dto.ArticleIdResponse;
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
  public ResponseEntity<ArticleIdResponse> getArticleBySlug(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(new ArticleIdResponse(article.getId(), article.getUserId())))
        .orElse(ResponseEntity.notFound().build());
  }
}
