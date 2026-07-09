package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Own-DB read side for articles/tags. Cross-service joins to {@code users} and {@code
 * article_favorites} were removed per the composition strategy — author/favorite filters are
 * resolved to ids by {@link io.spring.application.ArticleQueryService} via network calls, then
 * pushed down here as {@code authorId} / {@code favoritedArticleIds}.
 */
@Mapper
public interface ArticleReadService {
  ArticleData findById(@Param("id") String id);

  ArticleData findBySlug(@Param("slug") String slug);

  List<String> queryArticles(
      @Param("tag") String tag,
      @Param("authorId") String authorId,
      @Param("favoritedArticleIds") List<String> favoritedArticleIds,
      @Param("page") Page page);

  int countArticle(
      @Param("tag") String tag,
      @Param("authorId") String authorId,
      @Param("favoritedArticleIds") List<String> favoritedArticleIds);

  List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

  List<ArticleData> findArticlesOfAuthors(
      @Param("authors") List<String> authors, @Param("page") Page page);

  int countFeedSize(@Param("authors") List<String> authors);
}
