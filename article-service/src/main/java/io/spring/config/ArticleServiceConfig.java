package io.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.contracts.security.DefaultJwtVerifier;
import io.spring.contracts.security.JwtVerifier;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
public class ArticleServiceConfig {

  @Bean
  public JwtVerifier jwtVerifier(@Value("${jwt.secret}") String secret) {
    return new DefaultJwtVerifier(secret);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    // The MVC ObjectMapper enables UNWRAP_ROOT_VALUE (for @JsonRootName request bodies). Inter-
    // service composition responses (maps/lists/summaries) are NOT root-wrapped, so give the
    // RestTemplate its own plain mapper to avoid unwrapping their top-level keys.
    restTemplate
        .getMessageConverters()
        .removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
    restTemplate
        .getMessageConverters()
        .add(new MappingJackson2HttpMessageConverter(new ObjectMapper()));
    return restTemplate;
  }
}
