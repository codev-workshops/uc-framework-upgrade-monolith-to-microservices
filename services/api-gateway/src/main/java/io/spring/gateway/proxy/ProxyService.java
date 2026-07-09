package io.spring.gateway.proxy;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Transparently relays the incoming HTTP request to a downstream service, preserving method, path,
 * query string, headers (including Authorization) and body, and returning the downstream
 * status/headers/body verbatim.
 */
@Component
public class ProxyService {

  private final RestTemplate restTemplate;

  public ProxyService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public ResponseEntity<byte[]> forward(HttpServletRequest request, String targetBaseUrl)
      throws IOException {
    return forward(request, targetBaseUrl, request.getRequestURI());
  }

  public ResponseEntity<byte[]> forward(
      HttpServletRequest request, String targetBaseUrl, String targetPath) throws IOException {
    byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
    HttpHeaders headers = copyRequestHeaders(request);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetBaseUrl).path(targetPath);
    if (request.getQueryString() != null) {
      builder.query(request.getQueryString());
    }
    URI uri = builder.build(true).toUri();

    ResponseEntity<byte[]> response =
        restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), byte[].class);
    return relayResponse(response);
  }

  private HttpHeaders copyRequestHeaders(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> names = request.getHeaderNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      if (isHopByHop(name)) {
        continue;
      }
      Enumeration<String> values = request.getHeaders(name);
      while (values.hasMoreElements()) {
        headers.add(name, values.nextElement());
      }
    }
    return headers;
  }

  private ResponseEntity<byte[]> relayResponse(ResponseEntity<byte[]> response) {
    HttpHeaders out = new HttpHeaders();
    response
        .getHeaders()
        .forEach(
            (name, values) -> {
              if (!isHopByHop(name)) {
                out.put(name, values);
              }
            });
    return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
  }

  private boolean isHopByHop(String name) {
    String n = name.toLowerCase();
    return n.equals("host")
        || n.equals("connection")
        || n.equals("content-length")
        || n.equals("transfer-encoding")
        || n.equals("keep-alive")
        || n.equals("te")
        || n.equals("trailer")
        || n.equals("upgrade")
        || n.equals("proxy-authorization")
        || n.equals("proxy-authenticate");
  }

  public List<String> noBody() {
    return Collections.emptyList();
  }
}
