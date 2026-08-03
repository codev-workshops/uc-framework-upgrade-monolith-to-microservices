package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleRepository.class})
public class MyBatisArticleRepositoryTest extends DbTestBase {
  @Autowired private ArticleRepository articleRepository;

  private Article article;

  @BeforeEach
  public void setUp() {
    article = new Article("test", "desc", "body", Arrays.asList("java", "spring"), "123");
  }

  @Test
  public void should_create_and_fetch_article_success() {
    articleRepository.save(article);
    Optional<Article> optional = articleRepository.findById(article.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), article);
    Assertions.assertEquals(optional.get().getUserId(), "123");
  }

  @Test
  public void should_update_and_fetch_article_success() {
    articleRepository.save(article);

    String newTitle = "new test 2";
    article.update(newTitle, "", "");
    articleRepository.save(article);
    Optional<Article> optional = articleRepository.findBySlug(article.getSlug());
    Assertions.assertTrue(optional.isPresent());
    Article fetched = optional.get();
    Assertions.assertEquals(fetched.getTitle(), newTitle);
    Assertions.assertNotEquals(fetched.getBody(), "");
  }

  @Test
  public void should_delete_article() {
    articleRepository.save(article);

    articleRepository.remove(article);
    Assertions.assertFalse(articleRepository.findById(article.getId()).isPresent());
  }
}
