package io.spring.favorite.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class ArticleServiceClient {
    private final RestTemplate restTemplate;
    private final String articleServiceUrl;

    public ArticleServiceClient(@Value("${article-service.url:http://localhost:8083}") String articleServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.articleServiceUrl = articleServiceUrl;
    }

    public String getArticleIdBySlug(String slug) {
        Map response = restTemplate.getForObject(articleServiceUrl + "/internal/articles/by-slug/" + slug, Map.class);
        return response != null ? (String) response.get("id") : null;
    }
}
