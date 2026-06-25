package io.spring.articleservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleRepository;
import io.spring.articleservice.service.ArticleCommandService;
import io.spring.articleservice.service.ArticleData;
import io.spring.articleservice.service.ArticleQueryService;
import io.spring.shared.data.UserData;
import io.spring.shared.exception.NoAuthorizationException;
import io.spring.shared.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserData user) {
    return articleQueryService
        .findBySlug(slug, user != null ? user.getId() : null)
        .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal UserData user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!user.getId().equals(article.getUserId())) {
                throw new NoAuthorizationException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(
                      article,
                      updateArticleParam.getTitle(),
                      updateArticleParam.getDescription(),
                      updateArticleParam.getBody());
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService
                          .findBySlug(updatedArticle.getSlug(), user.getId())
                          .get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserData user) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!user.getId().equals(article.getUserId())) {
                throw new NoAuthorizationException();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}

@Getter
@JsonRootName("article")
@NoArgsConstructor
class UpdateArticleParam {
  private String title;
  private String description;
  private String body;
}
