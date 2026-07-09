package io.spring.infrastructure.clients;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** Builds outbound headers that forward the caller's {@code Authorization} header. */
final class ClientHeaders {
  private ClientHeaders() {}

  static HttpHeaders forwarding(String authorization) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (authorization != null && !authorization.isEmpty()) {
      headers.set(HttpHeaders.AUTHORIZATION, authorization);
    }
    return headers;
  }
}
