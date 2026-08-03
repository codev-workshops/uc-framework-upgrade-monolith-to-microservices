package io.spring.api;

import io.spring.application.TagWriteService;
import io.spring.application.TagsQueryService;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inter-service endpoints backing the {@code TagServiceClient} contract, so other services can
 * resolve tag lists without reading the {@code tags} / {@code article_tags} tables.
 */
@RestController
@RequestMapping(path = "/internal")
@AllArgsConstructor
public class InternalTagsApi {

  private TagsQueryService tagsQueryService;
  private TagWriteService tagWriteService;

  @GetMapping("/tags")
  public ResponseEntity<List<String>> allTags() {
    return ResponseEntity.ok(tagsQueryService.allTags());
  }

  @GetMapping("/articles/{articleId}/tags")
  public ResponseEntity<List<String>> tagsOfArticle(@PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(tagsQueryService.tagsOfArticle(articleId));
  }

  @GetMapping("/article-tags")
  public ResponseEntity<Map<String, List<String>>> tagsOfArticles(
      @RequestParam("articleIds") List<String> articleIds) {
    return ResponseEntity.ok(tagsQueryService.tagsOfArticles(articleIds));
  }

  @PutMapping("/articles/{articleId}/tags")
  public ResponseEntity<Void> setArticleTags(
      @PathVariable("articleId") String articleId, @RequestBody List<String> tagNames) {
    tagWriteService.setArticleTags(articleId, tagNames);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/articles/{articleId}/tags")
  public ResponseEntity<Void> deleteArticleTags(@PathVariable("articleId") String articleId) {
    tagWriteService.removeArticleTags(articleId);
    return ResponseEntity.noContent().build();
  }
}
