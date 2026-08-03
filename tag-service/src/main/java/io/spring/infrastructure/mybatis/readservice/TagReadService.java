package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleTagData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagReadService {
  List<String> all();

  List<String> tagsOfArticle(@Param("articleId") String articleId);

  List<ArticleTagData> tagsOfArticles(@Param("articleIds") List<String> articleIds);
}
