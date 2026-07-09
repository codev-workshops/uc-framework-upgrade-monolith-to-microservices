package io.spring.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(15000);
    // Allow us to inspect non-2xx responses instead of throwing on 4xx/5xx.
    RestTemplate restTemplate = new RestTemplate(factory);
    restTemplate.setErrorHandler(new NoopResponseErrorHandler());
    return restTemplate;
  }
}
