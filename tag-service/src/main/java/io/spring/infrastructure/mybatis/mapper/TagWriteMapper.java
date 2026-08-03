package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Write side of the {@code tags} / {@code article_tags} tables owned by tag-service. */
@Mapper
public interface TagWriteMapper {
  Tag findByName(@Param("name") String name);

  void insertTag(@Param("tag") Tag tag);

  void insertArticleTag(@Param("articleId") String articleId, @Param("tagId") String tagId);

  void deleteArticleTags(@Param("articleId") String articleId);
}
