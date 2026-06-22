package io.spring.tag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("Authorization", "Cache-Control", "Content-Type");
    }
}
