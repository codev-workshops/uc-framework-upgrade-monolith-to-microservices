package io.spring.shared.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

  @Bean
  public JwtUtils jwtUtils(JwtProperties jwtProperties) {
    return new JwtUtils(jwtProperties.getSecret());
  }
}
