package io.spring.contracts.client;

import java.util.List;
import java.util.Map;

/** Contract for tag lists and article-tag associations from tag-service. */
public interface TagServiceClient {
  List<String> allTags();

  List<String> tagsOfArticle(String articleId);

  /** articleId -> its tag names, for a batch of articles. */
  Map<String, List<String>> tagsOfArticles(List<String> articleIds);
}
