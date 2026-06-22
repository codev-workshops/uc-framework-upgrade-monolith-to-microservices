package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"io.spring.articleservice", "io.spring.common"})
@EnableFeignClients(basePackages = "io.spring.common.client")
public class ArticleServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
