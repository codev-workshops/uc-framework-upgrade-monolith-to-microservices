package io.spring.favoriteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"io.spring.favoriteservice", "io.spring.common"})
@EnableFeignClients(basePackages = "io.spring.common.client")
public class FavoriteServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FavoriteServiceApplication.class, args);
  }
}
