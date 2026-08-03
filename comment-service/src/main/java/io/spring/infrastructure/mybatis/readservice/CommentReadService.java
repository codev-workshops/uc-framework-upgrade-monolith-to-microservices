package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.CommentData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Reads from the {@code comments} table only; author data is composed from other services. */
@Mapper
public interface CommentReadService {
  CommentData findById(@Param("id") String id);

  List<CommentData> findByArticleId(@Param("articleId") String articleId);

  int countByArticleId(@Param("articleId") String articleId);
}
