package io.spring.gateway.graphql.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.gateway.config.ServiceUrls;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Thin HTTP client the GraphQL datafetchers use to fan out to the downstream services, forwarding
 * the current request's Authorization header. Mirrors the monolith's in-process query/command
 * services, but over REST.
 */
@Component
public class GatewayClient {

  private final RestTemplate restTemplate;
  private final ServiceUrls services;
  private final RequestAuthContext auth;
  private final ObjectMapper objectMapper;

  public GatewayClient(
      RestTemplate restTemplate,
      ServiceUrls services,
      RequestAuthContext auth,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.services = services;
    this.auth = auth;
    this.objectMapper = objectMapper;
  }

  // ----- generic helpers -----
  public ResponseEntity<String> exchange(HttpMethod method, String url, String jsonBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (auth.getAuthorization() != null) {
      headers.set(HttpHeaders.AUTHORIZATION, auth.getAuthorization());
    }
    return restTemplate.exchange(url, method, new HttpEntity<>(jsonBody, headers), String.class);
  }

  public JsonNode json(ResponseEntity<String> response) {
    try {
      if (response.getBody() == null || response.getBody().isEmpty()) {
        return objectMapper.createObjectNode();
      }
      return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse downstream response", e);
    }
  }

  public JsonNode getJson(String url) {
    return json(exchange(HttpMethod.GET, url, null));
  }

  // ----- article-service -----
  public String articleUrl() {
    return services.getArticle().getUrl();
  }

  public Optional<JsonNode> articleBySlug(String slug) {
    ResponseEntity<String> res = exchange(HttpMethod.GET, articleUrl() + "/articles/" + slug, null);
    if (!res.getStatusCode().is2xxSuccessful()) {
      return Optional.empty();
    }
    return Optional.of(json(res).path("article"));
  }

  public Optional<JsonNode> articleById(String id) {
    ResponseEntity<String> res =
        exchange(HttpMethod.GET, articleUrl() + "/internal/articles/" + id + "/data", null);
    if (!res.getStatusCode().is2xxSuccessful()) {
      return Optional.empty();
    }
    return Optional.of(json(res).path("article"));
  }

  public Optional<String> resolveArticleId(String slug) {
    ResponseEntity<String> res =
        exchange(HttpMethod.GET, articleUrl() + "/internal/articles/" + slug, null);
    if (!res.getStatusCode().is2xxSuccessful()) {
      return Optional.empty();
    }
    return Optional.ofNullable(json(res).path("id").asText(null));
  }

  public JsonNode listArticles(
      Integer offset, Integer limit, String tag, String author, String favoritedBy, boolean feed) {
    UriComponentsBuilder b =
        UriComponentsBuilder.fromHttpUrl(articleUrl() + (feed ? "/articles/feed" : "/articles"));
    if (offset != null) b.queryParam("offset", offset);
    if (limit != null) b.queryParam("limit", limit);
    if (tag != null) b.queryParam("tag", tag);
    if (author != null) b.queryParam("author", author);
    if (favoritedBy != null) b.queryParam("favorited", favoritedBy);
    return getJson(b.toUriString());
  }

  public java.util.List<String> tags() {
    JsonNode node = getJson(articleUrl() + "/tags").path("tags");
    java.util.List<String> tags = new java.util.ArrayList<>();
    node.forEach(t -> tags.add(t.asText()));
    return tags;
  }

  // ----- comment-service -----
  public JsonNode listComments(String slug) {
    return getJson(services.getComment().getUrl() + "/articles/" + slug + "/comments")
        .path("comments");
  }

  // ----- profile-service -----
  public Optional<JsonNode> profile(String username) {
    ResponseEntity<String> res =
        exchange(HttpMethod.GET, services.getProfile().getUrl() + "/profiles/" + username, null);
    if (!res.getStatusCode().is2xxSuccessful()) {
      return Optional.empty();
    }
    return Optional.of(json(res).path("profile"));
  }

  public RestTemplate restTemplate() {
    return restTemplate;
  }

  public ServiceUrls services() {
    return services;
  }

  public RequestAuthContext auth() {
    return auth;
  }

  public ObjectMapper mapper() {
    return objectMapper;
  }
}
