package io.spring.shared.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
  private String secret;
  private int sessionTime = 86400;
}
