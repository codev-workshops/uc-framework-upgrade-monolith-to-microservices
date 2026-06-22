package io.spring.common.client;

import io.spring.common.dto.ArticleIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "article-service", url = "${services.article-service.url:http://localhost:8082}")
public interface ArticleServiceClient {

  @GetMapping("/internal/articles/by-slug/{slug}")
  ArticleIdResponse getArticleBySlug(@PathVariable("slug") String slug);
}
