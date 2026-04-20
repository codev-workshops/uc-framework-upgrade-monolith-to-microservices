package io.spring.articleservice.application.article;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, String> {

  @Autowired private ArticleRepository articleRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return !articleRepository.findBySlug(Article.toSlug(value)).isPresent();
  }
}
