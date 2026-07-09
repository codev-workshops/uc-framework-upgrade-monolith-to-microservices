package io.spring.gateway.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.gateway.config.ServiceUrls;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Recomposes the favorite/unfavorite endpoints. favorite-service returns favorite STATE only, so
 * the gateway resolves slug->id via article-service, mutates favorite state, then asks
 * article-service to recompose the full ArticleData — matching the monolith response shape `{
 * "article": { ... } }`.
 */
@RestController
public class FavoriteGatewayController {

  private final RestTemplate restTemplate;
  private final ServiceUrls services;
  private final ObjectMapper objectMapper;

  public FavoriteGatewayController(
      RestTemplate restTemplate, ServiceUrls services, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.services = services;
    this.objectMapper = objectMapper;
  }

  @PostMapping("/articles/{slug}/favorite")
  public ResponseEntity<byte[]> favorite(
      @PathVariable("slug") String slug, HttpServletRequest request) {
    return mutateAndRecompose(slug, HttpMethod.POST, request);
  }

  @DeleteMapping("/articles/{slug}/favorite")
  public ResponseEntity<byte[]> unfavorite(
      @PathVariable("slug") String slug, HttpServletRequest request) {
    return mutateAndRecompose(slug, HttpMethod.DELETE, request);
  }

  private ResponseEntity<byte[]> mutateAndRecompose(
      String slug, HttpMethod method, HttpServletRequest request) {
    HttpHeaders auth = authHeaders(request);

    // 1. resolve slug -> article id
    ResponseEntity<String> ref =
        restTemplate.exchange(
            services.getArticle().getUrl() + "/internal/articles/" + slug,
            HttpMethod.GET,
            new HttpEntity<>(auth),
            String.class);
    if (!ref.getStatusCode().is2xxSuccessful()) {
      return ResponseEntity.status(ref.getStatusCode()).build();
    }
    String articleId;
    try {
      JsonNode node = objectMapper.readTree(ref.getBody());
      articleId = node.path("id").asText();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    // 2. mutate favorite state
    ResponseEntity<String> mutate =
        restTemplate.exchange(
            services.getFavorite().getUrl() + "/articles/" + articleId + "/favorite",
            method,
            new HttpEntity<>(auth),
            String.class);
    if (!mutate.getStatusCode().is2xxSuccessful()) {
      return ResponseEntity.status(mutate.getStatusCode()).build();
    }

    // 3. recompose the full ArticleData for the current user
    ResponseEntity<byte[]> recomposed =
        restTemplate.exchange(
            services.getArticle().getUrl() + "/internal/articles/" + articleId + "/data",
            HttpMethod.GET,
            new HttpEntity<>(auth),
            byte[].class);
    HttpHeaders out = new HttpHeaders();
    out.setContentType(MediaType.APPLICATION_JSON);
    return new ResponseEntity<>(recomposed.getBody(), out, recomposed.getStatusCode());
  }

  private HttpHeaders authHeaders(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null) {
      headers.set(HttpHeaders.AUTHORIZATION, authorization);
    }
    return headers;
  }
}
