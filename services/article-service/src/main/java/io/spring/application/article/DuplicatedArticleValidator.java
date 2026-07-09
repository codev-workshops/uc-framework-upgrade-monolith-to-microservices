package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, String> {

  @Autowired private ArticleRepository articleRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return !articleRepository.findBySlug(Article.toSlug(value)).isPresent();
  }
}
