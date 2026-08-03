package io.spring.config;

import io.spring.contracts.security.DefaultJwtVerifier;
import io.spring.contracts.security.JwtVerifier;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
public class CommentServiceConfig {

  @Bean
  public JwtVerifier jwtVerifier(@Value("${jwt.secret}") String secret) {
    return new DefaultJwtVerifier(secret);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(5))
        .build();
  }
}
