package io.spring.application.tag;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MybatisTest
@Import(TagsQueryService.class)
public class TagsQueryServiceTest {

  @Autowired private TagsQueryService tagsQueryService;

  @Test
  public void should_get_all_tags() {
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("java"));
    Assertions.assertEquals(7, tags.size());
  }

  @Test
  public void should_get_tags_of_article() {
    Assertions.assertEquals(
        Arrays.asList("java", "spring-boot", "tutorial"),
        tagsQueryService.tagsOfArticle("article-1"));
  }

  @Test
  public void should_get_empty_tags_for_unknown_article() {
    Assertions.assertTrue(tagsQueryService.tagsOfArticle("nope").isEmpty());
  }

  @Test
  public void should_get_tags_of_articles_in_batch() {
    Map<String, List<String>> tags =
        tagsQueryService.tagsOfArticles(Arrays.asList("article-1", "article-3", "unknown"));
    Assertions.assertEquals(
        Arrays.asList("java", "spring-boot", "tutorial"), tags.get("article-1"));
    Assertions.assertEquals(
        new java.util.HashSet<>(Arrays.asList("spring-boot", "best-practices", "microservices")),
        new java.util.HashSet<>(tags.get("article-3")));
    Assertions.assertTrue(tags.get("unknown").isEmpty());
  }

  @Test
  public void should_return_empty_map_for_no_articles() {
    Assertions.assertTrue(
        tagsQueryService.tagsOfArticles(java.util.Collections.emptyList()).isEmpty());
  }
}
