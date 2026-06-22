package io.spring.commentservice.api.security;

import io.spring.common.security.BaseWebSecurityConfig;
import io.spring.common.security.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends BaseWebSecurityConfig {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Bean
  public JwtTokenFilter jwtTokenFilterBean() {
    return new JwtTokenFilter(jwtSecret);
  }

  @Override
  protected JwtTokenFilter jwtTokenFilter() {
    return jwtTokenFilterBean();
  }

  @Override
  protected void configureAuthorization(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(HttpMethod.GET, "/articles/*/comments", "/articles/*/comments/**")
        .permitAll()
        .antMatchers("/internal/**")
        .permitAll()
        .anyRequest()
        .authenticated();
  }
}
