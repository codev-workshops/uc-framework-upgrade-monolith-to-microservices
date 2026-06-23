package io.spring.tagservice.application;

import io.spring.tagservice.infrastructure.DbTestBase;
import io.spring.tagservice.infrastructure.TagReadService;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({TagsQueryService.class})
public class TagsQueryServiceTest extends DbTestBase {
  @Autowired private TagsQueryService tagsQueryService;

  @Autowired private TagReadService tagReadService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void should_get_all_tags() {
    String tagId = UUID.randomUUID().toString();
    jdbcTemplate.update("INSERT INTO tags (id, name) VALUES (?, ?)", tagId, "java");
    Assertions.assertTrue(tagsQueryService.allTags().contains("java"));
  }
}
