package io.spring.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.spring.commentservice", "io.spring.shared"})
public class CommentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommentServiceApplication.class, args);
  }
}
