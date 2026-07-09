package io.spring.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Base URLs of the downstream microservices, bound from `services.*.url`. */
@Component
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
public class ServiceUrls {
  private Endpoint userAuth = new Endpoint();
  private Endpoint profile = new Endpoint();
  private Endpoint article = new Endpoint();
  private Endpoint favorite = new Endpoint();
  private Endpoint comment = new Endpoint();

  @Getter
  @Setter
  public static class Endpoint {
    private String url;
  }
}
