package io.spring.gateway.proxy;

import io.spring.gateway.config.ServiceUrls;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Restores the monolith's public REST surface, routing each path to the owning microservice. Path
 * patterns are non-overlapping by segment count / literal, so Spring resolves them unambiguously
 * (e.g. /articles/{slug}/comments and /articles/{slug}/favorite are more specific than
 * /articles/{slug}). Favorite is handled separately (recomposition).
 */
@RestController
public class RestGatewayController {

  private final ProxyService proxy;
  private final ServiceUrls services;

  public RestGatewayController(ProxyService proxy, ServiceUrls services) {
    this.proxy = proxy;
    this.services = services;
  }

  // ---- user-auth-service ----
  @PostMapping({"/users", "/users/login"})
  public ResponseEntity<byte[]> users(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getUserAuth().getUrl());
  }

  @GetMapping("/user")
  public ResponseEntity<byte[]> currentUser(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getUserAuth().getUrl());
  }

  @PutMapping("/user")
  public ResponseEntity<byte[]> updateUser(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getUserAuth().getUrl());
  }

  // ---- profile-service ----
  @GetMapping("/profiles/{username}")
  public ResponseEntity<byte[]> getProfile(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getProfile().getUrl());
  }

  @PostMapping("/profiles/{username}/follow")
  public ResponseEntity<byte[]> follow(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getProfile().getUrl());
  }

  @DeleteMapping("/profiles/{username}/follow")
  public ResponseEntity<byte[]> unfollow(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getProfile().getUrl());
  }

  // ---- article-service ----
  @GetMapping("/tags")
  public ResponseEntity<byte[]> tags(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  @GetMapping({"/articles", "/articles/feed"})
  public ResponseEntity<byte[]> listArticles(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  @PostMapping("/articles")
  public ResponseEntity<byte[]> createArticle(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  @GetMapping("/articles/{slug}")
  public ResponseEntity<byte[]> getArticle(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  @PutMapping("/articles/{slug}")
  public ResponseEntity<byte[]> updateArticle(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  @DeleteMapping("/articles/{slug}")
  public ResponseEntity<byte[]> deleteArticle(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getArticle().getUrl());
  }

  // ---- comment-service ----
  @GetMapping("/articles/{slug}/comments")
  public ResponseEntity<byte[]> getComments(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getComment().getUrl());
  }

  @PostMapping("/articles/{slug}/comments")
  public ResponseEntity<byte[]> addComment(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getComment().getUrl());
  }

  @DeleteMapping("/articles/{slug}/comments/{id}")
  public ResponseEntity<byte[]> deleteComment(HttpServletRequest request) throws IOException {
    return proxy.forward(request, services.getComment().getUrl());
  }
}
