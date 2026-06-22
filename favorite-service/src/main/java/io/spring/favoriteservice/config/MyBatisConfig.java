package io.spring.favoriteservice.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
  "io.spring.favoriteservice.infrastructure.mybatis.mapper",
  "io.spring.favoriteservice.infrastructure.mybatis.readservice"
})
public class MyBatisConfig {}
