package io.spring.infrastructure.clients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

  /**
   * Dedicated {@link RestTemplate} for internal service-to-service calls. It uses its own {@link
   * ObjectMapper} so the application-wide {@code UNWRAP_ROOT_VALUE} setting (needed for the public
   * {@code {"comment": ...}} request bodies) does not leak into internal payload deserialization.
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return builder
        .messageConverters(
            new MappingJackson2HttpMessageConverter(mapper), new StringHttpMessageConverter())
        .build();
  }
}
