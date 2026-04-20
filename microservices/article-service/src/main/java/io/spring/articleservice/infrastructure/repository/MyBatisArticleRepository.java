package io.spring.articleservice.infrastructure.repository;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {
  private ArticleMapper articleMapper;

  public MyBatisArticleRepository(ArticleMapper articleMapper) {
    this.articleMapper = articleMapper;
  }

  @Override
  public void save(Article article) {
    if (articleMapper.findById(article.getId()) == null) {
      createNew(article);
    } else {
      articleMapper.update(article);
    }
  }

  private void createNew(Article article) {
    for (Tag tag : article.getTags()) {
      Tag targetTag = articleMapper.findTag(tag.getName());
      if (targetTag == null) {
        articleMapper.insertTag(tag);
      } else {
        tag.setId(targetTag.getId());
      }
      articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
    }
    articleMapper.insert(article);
  }

  @Override
  public Optional<Article> findById(String id) {
    return Optional.ofNullable(articleMapper.findById(id));
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  @Override
  public void remove(Article article) {
    articleMapper.delete(article.getId());
  }
}
