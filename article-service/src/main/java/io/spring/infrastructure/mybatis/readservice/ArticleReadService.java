package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Reads restricted to the {@code articles} table this service owns. The monolith's joins against
 * tags/favorites/users are dropped here and replaced by API composition in {@link
 * io.spring.application.ArticleQueryService}.
 */
@Mapper
public interface ArticleReadService {
  ArticleData findById(@Param("id") String id);

  ArticleData findBySlug(@Param("slug") String slug);

  /** All articles, newest first (composition filters/paginates in memory over this small set). */
  List<ArticleData> findAll();

  /** Articles authored by any of {@code authors}, newest first. */
  List<ArticleData> findByAuthors(@Param("authors") List<String> authors);
}
