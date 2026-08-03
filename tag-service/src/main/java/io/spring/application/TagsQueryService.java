package io.spring.application;

import io.spring.application.data.ArticleTagData;
import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  public List<String> allTags() {
    return tagReadService.all();
  }

  public List<String> tagsOfArticle(String articleId) {
    return tagReadService.tagsOfArticle(articleId);
  }

  public Map<String, List<String>> tagsOfArticles(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> result = new LinkedHashMap<>();
    for (String articleId : articleIds) {
      result.put(articleId, new java.util.ArrayList<>());
    }
    for (ArticleTagData data : tagReadService.tagsOfArticles(articleIds)) {
      result
          .computeIfAbsent(data.getArticleId(), key -> new java.util.ArrayList<>())
          .add(data.getTagName());
    }
    return result.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
  }
}
