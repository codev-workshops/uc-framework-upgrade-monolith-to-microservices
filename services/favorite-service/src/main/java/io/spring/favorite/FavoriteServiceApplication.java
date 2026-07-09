package io.spring.favorite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("io.spring.favorite.infrastructure.mybatis")
public class FavoriteServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FavoriteServiceApplication.class, args);
  }
}
