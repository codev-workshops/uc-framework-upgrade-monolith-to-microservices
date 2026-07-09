package io.spring.favorite.infrastructure.mybatis.readservice;

import io.spring.favorite.application.data.ArticleFavoriteCount;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ArticleFavoritesReadService {
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  int articleFavoriteCount(@Param("articleId") String articleId);

  List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

  List<String> userFavorites(@Param("ids") List<String> ids, @Param("userId") String userId);

  List<String> articlesFavoritedByUser(@Param("userId") String userId);
}
