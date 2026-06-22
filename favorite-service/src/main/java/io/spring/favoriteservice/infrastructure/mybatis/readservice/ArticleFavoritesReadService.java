package io.spring.favoriteservice.infrastructure.mybatis.readservice;

import io.spring.favoriteservice.core.ArticleFavorite;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoritesReadService {
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  int articleFavoriteCount(@Param("articleId") String articleId);

  Set<String> userFavorites(@Param("userId") String userId, @Param("ids") List<String> ids);

  List<ArticleFavorite> articlesFavoriteCount(@Param("ids") List<String> ids);
}
