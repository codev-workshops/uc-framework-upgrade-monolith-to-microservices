package io.spring.commentservice.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"io.spring.commentservice.infrastructure.mybatis.mapper",
    "io.spring.commentservice.infrastructure.mybatis.readservice"})
public class MyBatisConfig {}
