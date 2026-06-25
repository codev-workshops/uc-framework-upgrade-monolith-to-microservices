package io.spring.articleservice.service;

import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  public Article createArticle(
      String title, String description, String body, List<String> tagList, String userId) {
    Article article = new Article(title, description, body, tagList, userId);
    articleRepository.save(article);
    return article;
  }

  public Article updateArticle(Article article, String title, String description, String body) {
    article.update(title, description, body);
    articleRepository.save(article);
    return article;
  }
}
