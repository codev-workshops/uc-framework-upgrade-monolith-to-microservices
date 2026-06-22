package io.spring.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"io.spring.commentservice", "io.spring.common"})
@EnableFeignClients(basePackages = "io.spring.common.client")
public class CommentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommentServiceApplication.class, args);
  }
}
