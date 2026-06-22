package io.spring.favoriteservice.infrastructure.repository;

import io.spring.favoriteservice.core.ArticleFavorite;
import io.spring.favoriteservice.core.ArticleFavoriteRepository;
import io.spring.favoriteservice.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper articleFavoriteMapper;

  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper articleFavoriteMapper) {
    this.articleFavoriteMapper = articleFavoriteMapper;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    if (articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId())
        == null) {
      articleFavoriteMapper.insert(articleFavorite);
    }
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(articleFavoriteMapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    articleFavoriteMapper.delete(favorite);
  }

  @Override
  public int countByArticleId(String articleId) {
    return articleFavoriteMapper.countByArticleId(articleId);
  }
}
