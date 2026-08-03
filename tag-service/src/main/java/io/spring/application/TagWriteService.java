package io.spring.application;

import io.spring.core.article.Tag;
import io.spring.infrastructure.mybatis.mapper.TagWriteMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists article-tag associations pushed by article-service on article create/update. Tag rows
 * are upserted by name; the {@code article_id} is a referenced id owned by article-service.
 */
@Service
@AllArgsConstructor
public class TagWriteService {
  private final TagWriteMapper tagWriteMapper;

  @Transactional
  public void setArticleTags(String articleId, List<String> tagNames) {
    tagWriteMapper.deleteArticleTags(articleId);
    if (tagNames == null) {
      return;
    }
    tagNames.stream()
        .distinct()
        .forEach(
            name -> {
              Tag tag =
                  Optional.ofNullable(tagWriteMapper.findByName(name))
                      .orElseGet(
                          () -> {
                            Tag created = new Tag(name);
                            tagWriteMapper.insertTag(created);
                            return created;
                          });
              tagWriteMapper.insertArticleTag(articleId, tag.getId());
            });
  }

  @Transactional
  public void removeArticleTags(String articleId) {
    tagWriteMapper.deleteArticleTags(articleId);
  }
}
