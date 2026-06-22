package io.spring.articleservice.api.security;

import io.spring.common.security.BaseWebSecurityConfig;
import io.spring.common.security.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends BaseWebSecurityConfig {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Bean
  @Override
  protected JwtTokenFilter jwtTokenFilter() {
    return new JwtTokenFilter(jwtSecret);
  }

  @Override
  protected void configureAuthorization(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .antMatchers(HttpMethod.GET, "/articles/feed")
        .authenticated()
        .antMatchers(HttpMethod.GET, "/articles/**", "/tags")
        .permitAll()
        .antMatchers("/internal/**")
        .permitAll()
        .anyRequest()
        .authenticated();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    return super.corsConfigurationSource();
  }
}
