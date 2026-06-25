package io.spring.gateway;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class GatewayController {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;
  private final String articleServiceUrl;
  private final String commentServiceUrl;
  private final String favoriteServiceUrl;

  public GatewayController(
      RestTemplate restTemplate,
      @Value("${services.user-service.url}") String userServiceUrl,
      @Value("${services.article-service.url}") String articleServiceUrl,
      @Value("${services.comment-service.url}") String commentServiceUrl,
      @Value("${services.favorite-service.url}") String favoriteServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
    this.articleServiceUrl = articleServiceUrl;
    this.commentServiceUrl = commentServiceUrl;
    this.favoriteServiceUrl = favoriteServiceUrl;
  }

  @RequestMapping("/users/**")
  public ResponseEntity<?> proxyUsers(HttpServletRequest request) throws IOException {
    return proxyRequest(request, userServiceUrl);
  }

  @RequestMapping("/user/**")
  public ResponseEntity<?> proxyUser(HttpServletRequest request) throws IOException {
    return proxyRequest(request, userServiceUrl);
  }

  @RequestMapping("/profiles/**")
  public ResponseEntity<?> proxyProfiles(HttpServletRequest request) throws IOException {
    return proxyRequest(request, userServiceUrl);
  }

  @RequestMapping("/tags/**")
  public ResponseEntity<?> proxyTags(HttpServletRequest request) throws IOException {
    return proxyRequest(request, articleServiceUrl);
  }

  @RequestMapping("/articles/{slug}/comments/**")
  public ResponseEntity<?> proxyComments(HttpServletRequest request) throws IOException {
    return proxyRequest(request, commentServiceUrl);
  }

  @RequestMapping("/articles/{slug}/favorite/**")
  public ResponseEntity<?> proxyFavorite(HttpServletRequest request) throws IOException {
    return proxyRequest(request, favoriteServiceUrl);
  }

  @RequestMapping("/articles/**")
  public ResponseEntity<?> proxyArticles(HttpServletRequest request) throws IOException {
    return proxyRequest(request, articleServiceUrl);
  }

  private ResponseEntity<?> proxyRequest(HttpServletRequest request, String targetBaseUrl)
      throws IOException {
    String path = request.getRequestURI();
    String query = request.getQueryString();

    URI uri =
        UriComponentsBuilder.fromUriString(targetBaseUrl)
            .path(path)
            .query(query)
            .build(true)
            .toUri();

    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (!headerName.equalsIgnoreCase("host")
          && !headerName.equalsIgnoreCase("content-length")) {
        headers.set(headerName, request.getHeader(headerName));
      }
    }

    byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
    HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());

    try {
      ResponseEntity<byte[]> response =
          restTemplate.exchange(uri, method, httpEntity, byte[].class);

      HttpHeaders responseHeaders = new HttpHeaders();
      response.getHeaders().forEach((key, value) -> {
        if (!key.equalsIgnoreCase("Transfer-Encoding")) {
          responseHeaders.put(key, value);
        }
      });
      return ResponseEntity.status(response.getStatusCode())
          .headers(responseHeaders)
          .body(response.getBody());
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode())
          .body(e.getResponseBodyAsString());
    }
  }
}
