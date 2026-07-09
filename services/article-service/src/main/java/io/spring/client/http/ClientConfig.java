package io.spring.client.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Provides the {@link RestTemplate} used by the HTTP upstream clients. An interceptor forwards the
 * incoming {@code Authorization} header on every outbound service-to-service call so downstream
 * services can resolve the current user identically (per the frozen JWT contract).
 */
@Configuration
public class ClientConfig {

  @Bean
  public RestTemplate upstreamRestTemplate(RestTemplateBuilder builder) {
    // Dedicated ObjectMapper: the app enables UNWRAP_ROOT_VALUE for @JsonRootName request bodies,
    // which must NOT apply to internal service payloads (plain objects/arrays).
    ObjectMapper mapper =
        new ObjectMapper()
            .disable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return builder
        .messageConverters(new MappingJackson2HttpMessageConverter(mapper))
        .additionalInterceptors(
            (request, body, execution) -> {
              String authorization = currentAuthorizationHeader();
              if (authorization != null
                  && !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, authorization);
              }
              return execution.execute(request, body);
            })
        .build();
  }

  private String currentAuthorizationHeader() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
    return null;
  }

  static HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(
        Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    return headers;
  }
}
