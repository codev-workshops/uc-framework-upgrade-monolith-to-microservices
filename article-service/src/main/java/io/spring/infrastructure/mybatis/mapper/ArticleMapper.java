package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.article.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Persistence for the {@code articles} table only; tag associations live in tag-service. */
@Mapper
public interface ArticleMapper {
  void insert(@Param("article") Article article);

  Article findById(@Param("id") String id);

  Article findBySlug(@Param("slug") String slug);

  void update(@Param("article") Article article);

  void delete(@Param("id") String id);
}
